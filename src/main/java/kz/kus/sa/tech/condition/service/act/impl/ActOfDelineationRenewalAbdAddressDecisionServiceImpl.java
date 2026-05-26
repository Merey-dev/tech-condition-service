package kz.kus.sa.tech.condition.service.act.impl;

import kz.kus.sa.auth.api.currentuser.CurrentUserApiService;
import kz.kus.sa.auth.api.provider.ProviderApiService;
import kz.kus.sa.auth.api.provider.dto.SubdivisionDto;
import kz.kus.sa.auth.api.user.UserApiService;
import kz.kus.sa.auth.api.user.dto.UserDto;
import kz.kus.sa.auth.api.user.dto.UserFilterDto;
import kz.kus.sa.registry.api.RegistryApiService;
import kz.kus.sa.registry.api.RegistrySignApiService;
import kz.kus.sa.registry.dto.common.AssignDto;
import kz.kus.sa.registry.dto.common.SignCreateDto;
import kz.kus.sa.registry.dto.renewal.ActOfDelineationRenewalDto;
import kz.kus.sa.registry.enums.Event;
import kz.kus.sa.registry.enums.Status;
import kz.kus.sa.report.api.ReportActOfDelineationRenewalApiService;
import kz.kus.sa.tech.condition.dao.entity.AbdAddressEntity;
import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationRenewalAbdAddressDecisionEntity;
import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationRenewalEntity;
import kz.kus.sa.tech.condition.dao.mapper.ActOfDelineationRenewalAbdAddressDecisionMapper;
import kz.kus.sa.tech.condition.dao.mapper.ActOfDelineationRenewalMapper;
import kz.kus.sa.tech.condition.dao.mapper.ExternalSubdivisionMapper;
import kz.kus.sa.tech.condition.dao.mapper.ExternalUserMapper;
import kz.kus.sa.tech.condition.dao.repository.AbdAddressRepository;
import kz.kus.sa.tech.condition.dao.repository.ActOfDelineationRenewalAbdAddressDecisionRepository;
import kz.kus.sa.tech.condition.dao.repository.ActOfDelineationRenewalRepository;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationRenewalAbdAddressDecisionDto;
import kz.kus.sa.tech.condition.enums.AbdAddressDecisionStatus;
import kz.kus.sa.tech.condition.exception.BadRequestException;
import kz.kus.sa.tech.condition.exception.BusinessException;
import kz.kus.sa.tech.condition.exception.ErrorCode;
import kz.kus.sa.tech.condition.exception.NotFoundException;
import kz.kus.sa.tech.condition.service.act.ActOfDelineationRenewalAbdAddressDecisionService;
import kz.kus.sa.tech.condition.service.report.ActOfDelineationReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ActOfDelineationRenewalAbdAddressDecisionServiceImpl
        implements ActOfDelineationRenewalAbdAddressDecisionService {

    private final ActOfDelineationRenewalAbdAddressDecisionRepository decisionRepository;
    private final ActOfDelineationRenewalRepository renewalRepository;
    private final AbdAddressRepository abdAddressRepository;
    private final ActOfDelineationRenewalAbdAddressDecisionMapper decisionMapper;
    private final ActOfDelineationRenewalMapper renewalMapper;
    private final ExternalUserMapper externalUserMapper;
    private final ExternalSubdivisionMapper externalSubdivisionMapper;
    private final UserApiService userApiService;
    private final ProviderApiService providerApiService;
    private final CurrentUserApiService currentUserApiService;
    private final RegistryApiService registryApiService;
    private final RegistrySignApiService registrySignApiService;
    private final ActOfDelineationReportService actOfDelineationReportService;
    private final ReportActOfDelineationRenewalApiService reportActOfDelineationRenewalApiService;

    // ─── ASSIGN ────────────────────────────────────────────────────────────

    @Override
    public void assign(UUID renewalId, AssignDto dto) {
        ActOfDelineationRenewalEntity renewal = findRenewalById(renewalId);

        if (Event.ASSIGN_TO_DIVISION_WITH_ADDRESS == dto.getEvent()) {
            assignDivisionsWithAddresses(renewal, dto);
        } else if (Event.ASSIGN_TO_EXECUTOR_WITH_ADDRESS == dto.getEvent()) {
            assignExecutorsWithAddresses(renewal, dto);
        } else {
            throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
        }
    }

    private void assignDivisionsWithAddresses(ActOfDelineationRenewalEntity renewal, AssignDto dto) {
        if (isEmpty(dto.getAddressDivisions())) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
        }
        dto.getAddressDivisions().forEach(d -> {
            SubdivisionDto subdivisionDto = providerApiService.getSubdivision(d.getDivision());

            List<UserDto> users = userApiService.searchAsList(UserFilterDto.builder()
                    .subDivisionIds(List.of(d.getDivision()))
                    .permissions(List.of(Event.TCA_TAKE_TO_EXECUTION.name()))
                    .build());
            List<UUID> assignees = users.stream()
                    .map(UserDto::getId)
                    .collect(Collectors.toList());

            d.getObjectAbdAddresses().forEach(abdAddressDto -> {
                ActOfDelineationRenewalAbdAddressDecisionEntity decision = decisionRepository
                        .findByActOfDelineationRenewalIdAndObjectAbdAddressId(renewal.getId(), abdAddressDto.getId())
                        .orElseGet(() -> newDecision(renewal, findAddressById(abdAddressDto.getId())));

                decision.setAssignedSubdivision(externalSubdivisionMapper.toEntity(subdivisionDto));
                decision.setAssignees(assignees);
                addRelatedUsers(decision, assignees);
                decisionRepository.save(decision);
            });
        });
    }

    private void assignExecutorsWithAddresses(ActOfDelineationRenewalEntity renewal, AssignDto dto) {
        if (isEmpty(dto.getAddressExecutors())) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
        }
        dto.getAddressExecutors().forEach(e -> {
            UserDto userDto = userApiService.getUserById(e.getExecutor());
            SubdivisionDto subdivisionDto = providerApiService.getSubdivision(userDto.getSubdivisionId());

            e.getObjectAbdAddresses().forEach(abdAddressDto -> {
                ActOfDelineationRenewalAbdAddressDecisionEntity decision = decisionRepository
                        .findByActOfDelineationRenewalIdAndObjectAbdAddressId(renewal.getId(), abdAddressDto.getId())
                        .orElseGet(() -> newDecision(renewal, findAddressById(abdAddressDto.getId())));

                decision.setAssignedExecutor(externalUserMapper.toEntity(userDto));
                decision.setAssignedSubdivision(externalSubdivisionMapper.toEntity(subdivisionDto));
                decision.setAssignees(List.of(e.getExecutor()));
                addRelatedUsers(decision, List.of(e.getExecutor()));
                decisionRepository.save(decision);
            });
        });
    }

    @Override
    public void bulkAssignAllAddresses(UUID renewalId, List<UUID> assignees, SubdivisionDto subdivisionForAll) {
        if (isEmpty(assignees)) {
            throw new BadRequestException(ErrorCode.NO_USERS_TO_ASSIGN.name());
        }
        ActOfDelineationRenewalEntity renewal = findRenewalById(renewalId);
        List<AbdAddressEntity> addresses = abdAddressRepository.findAllByActOfDelineationRenewalId(renewalId);
        if (isEmpty(addresses)) {
            log.warn("AOD RENEWAL [BULK ASSIGN]: renewal id=[{}] has no addresses", renewalId);
            return;
        }

        addresses.forEach(address -> {
            ActOfDelineationRenewalAbdAddressDecisionEntity decision = decisionRepository
                    .findByActOfDelineationRenewalIdAndObjectAbdAddressId(renewalId, address.getId())
                    .orElseGet(() -> newDecision(renewal, address));

            if (nonNull(subdivisionForAll)) {
                decision.setAssignedSubdivision(externalSubdivisionMapper.toEntity(subdivisionForAll));
            }
            decision.setAssignees(new ArrayList<>(assignees));
            addRelatedUsers(decision, assignees);
            decisionRepository.save(decision);
        });
        log.info("AOD RENEWAL [BULK ASSIGN]: renewal id=[{}], decisions count=[{}]", renewalId, addresses.size());
    }

    private ActOfDelineationRenewalAbdAddressDecisionEntity newDecision(ActOfDelineationRenewalEntity renewal,
                                                                         AbdAddressEntity address) {
        ActOfDelineationRenewalAbdAddressDecisionEntity d = new ActOfDelineationRenewalAbdAddressDecisionEntity();
        d.setActOfDelineationRenewal(renewal);
        d.setObjectAbdAddress(address);
        d.setStatusCode(AbdAddressDecisionStatus.ASSIGNED.getCode());
        return d;
    }

    // ─── EVENTS ────────────────────────────────────────────────────────────

    @Override
    public void takeToExecution(UUID decisionId) {
        ActOfDelineationRenewalAbdAddressDecisionEntity decision = findById(decisionId);
        ActOfDelineationRenewalEntity renewal = decision.getActOfDelineationRenewal();

        ensureStatus(decision, AbdAddressDecisionStatus.ASSIGNED);
        ensureExecutor(decision);

        var currentUser = currentUserApiService.getCurrentUser();
        decision.setExecutor(externalUserMapper.fromCurrentUserResponse(currentUser));
        decision.setAssignees(List.of(currentUser.getId()));
        addRelatedUser(decision, currentUser.getId());
        decision.setStatusCode(AbdAddressDecisionStatus.ON_EXECUTION.getCode());

        decisionRepository.save(decision);

        // Если это первый decision взятый в работу — продвигаем заявление
        boolean anyOnExecution = decisionRepository
                .existsByActOfDelineationRenewalIdAndStatusCodeNot(
                        renewal.getId(), AbdAddressDecisionStatus.ASSIGNED.getCode());

        if (!anyOnExecution
                && Status.ASSIGNED.getCode().equals(renewal.getStatusCode())) {
            renewal.setStatusCode(Status.ON_EXECUTION.getCode());
            if (isNull(renewal.getExecutor())) {
                renewal.setExecutor(externalUserMapper.fromCurrentUserResponse(currentUser));
                renewal.setAssignees(List.of(currentUser.getId()));
            }
            renewalRepository.save(renewal);
            updateRegistryData(renewal, Event.TAKE_TO_EXECUTION);
        }

        log.info("AOD RENEWAL DECISION [TAKE TO EXECUTION]: decisionId=[{}]", decisionId);
    }

    @Override
    public void sendForApproval(UUID decisionId, AssignDto dto) {
        ActOfDelineationRenewalAbdAddressDecisionEntity decision = findById(decisionId);

        if (isEmpty(dto.getExecutors())) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
        }
        ensureStatus(decision, AbdAddressDecisionStatus.ON_EXECUTION,
                AbdAddressDecisionStatus.RETURNED_FOR_REVISION,
                AbdAddressDecisionStatus.EXECUTED);
        ensureExecutor(decision);

        decision.setAssignees(new ArrayList<>(dto.getExecutors()));
        addRelatedUsers(decision, dto.getExecutors());
        decision.setStatusCode(AbdAddressDecisionStatus.UNDER_APPROVAL.getCode());
        decisionRepository.save(decision);

        log.info("AOD RENEWAL DECISION [SEND FOR APPROVAL]: decisionId=[{}], assignees=[{}]",
                decisionId, dto.getExecutors());
    }

    @Override
    public void sendForRevision(UUID decisionId, String reason) {
        ActOfDelineationRenewalAbdAddressDecisionEntity decision = findById(decisionId);

        if (reason == null || reason.isEmpty()) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
        }
        ensureStatus(decision, AbdAddressDecisionStatus.UNDER_APPROVAL,
                AbdAddressDecisionStatus.AT_SIGNING);
        ensureExecutor(decision);

        decision.setRevisionReason(reason);
        decision.setStatusCode(AbdAddressDecisionStatus.RETURNED_FOR_REVISION.getCode());

        if (decision.getExecutor() != null) {
            decision.setAssignees(List.of(decision.getExecutor().getId()));
        }
        decisionRepository.save(decision);

        log.info("AOD RENEWAL DECISION [SEND FOR REVISION]: decisionId=[{}], reason=[{}]", decisionId, reason);
    }

    @Override
    public void sendForConfirmation(UUID decisionId, AssignDto dto) {
        ActOfDelineationRenewalAbdAddressDecisionEntity decision = findById(decisionId);

        if (isEmpty(dto.getExecutors())) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
        }
        ensureStatus(decision, AbdAddressDecisionStatus.UNDER_APPROVAL,
                AbdAddressDecisionStatus.APPROVED);
        ensureExecutor(decision);

        var currentUser = currentUserApiService.getCurrentUser();
        decision.setManager(externalUserMapper.fromCurrentUserResponse(currentUser));
        decision.setManagerApprovedDatetime(OffsetDateTime.now());

        UserDto userDto = userApiService.getUserById(dto.getExecutors().get(0));
        decision.setDirector(externalUserMapper.toEntity(userDto));

        decision.setAssignees(new ArrayList<>(dto.getExecutors()));
        addRelatedUsers(decision, dto.getExecutors());
        decision.setStatusCode(AbdAddressDecisionStatus.AT_SIGNING.getCode());
        decisionRepository.save(decision);

        log.info("AOD RENEWAL DECISION [SEND FOR CONFIRMATION]: decisionId=[{}], assignees=[{}]",
                decisionId, dto.getExecutors());
    }

    @Override
    public void providerSign(UUID decisionId, SignCreateDto sign) {
        ActOfDelineationRenewalAbdAddressDecisionEntity decision = findById(decisionId);
        ActOfDelineationRenewalEntity renewal = decision.getActOfDelineationRenewal();

        ensureStatus(decision, AbdAddressDecisionStatus.AT_SIGNING);
        ensureExecutor(decision);

        var currentUser = currentUserApiService.getCurrentUser();
        decision.setDirector(externalUserMapper.fromCurrentUserResponse(currentUser));
        decision.setProviderSignedDatetime(OffsetDateTime.now());

        var actReportData = actOfDelineationReportService.actReportData(renewal.getId());
        sign.setBase64Data(reportActOfDelineationRenewalApiService
                .actOfDelineationReportBase64(renewal.getId(), actReportData));
        registrySignApiService.addProviderSign(renewal.getStatementId(), sign);

        decision.setStatusCode(AbdAddressDecisionStatus.SIGNED.getCode());
        decisionRepository.save(decision);

        // Зеркалим на заявление (для совместимости со старыми отчётами)
        renewal.setDirector(externalUserMapper.fromCurrentUserResponse(currentUser));
        renewal.setDirectorApprovedDatetime(OffsetDateTime.now());

        checkAllDecisionsCompleted(renewal);

        log.info("AOD RENEWAL DECISION [PROVIDER SIGN]: decisionId=[{}]", decisionId);
    }

    @Override
    public void consumerSign(UUID decisionId, SignCreateDto sign) {
        ActOfDelineationRenewalAbdAddressDecisionEntity decision = findById(decisionId);
        ActOfDelineationRenewalEntity renewal = decision.getActOfDelineationRenewal();

        ensureStatus(decision, AbdAddressDecisionStatus.SIGNED, AbdAddressDecisionStatus.AT_SIGNING);

        registrySignApiService.addConsumerSign(renewal.getStatementId(), sign);

        decision.setConsumerSignedDatetime(OffsetDateTime.now());
        if (sign.getType() != null) {
            decision.setConsumerSignedType(sign.getType());
        }
        decision.setStatusCode(AbdAddressDecisionStatus.SIGNED.getCode());
        decisionRepository.save(decision);

        checkAllDecisionsCompleted(renewal);

        log.info("AOD RENEWAL DECISION [CONSUMER SIGN]: decisionId=[{}]", decisionId);
    }

    private void checkAllDecisionsCompleted(ActOfDelineationRenewalEntity renewal) {
        boolean allSigned = !decisionRepository
                .existsByActOfDelineationRenewalIdAndStatusCodeNot(
                        renewal.getId(), AbdAddressDecisionStatus.SIGNED.getCode());

        if (allSigned && !Status.COMPLETED.getCode().equals(renewal.getStatusCode())) {
            renewal.setStatusCode(Status.COMPLETED.getCode());
            renewalRepository.save(renewal);
            updateRegistryData(renewal, Event.CONSUMER_SIGN);
            log.info("AOD RENEWAL [COMPLETED]: id=[{}]", renewal.getId());
        }
    }

    // ─── QUERIES ───────────────────────────────────────────────────────────

    @Override
    public ActOfDelineationRenewalAbdAddressDecisionEntity findById(UUID decisionId) {
        return decisionRepository.findById(decisionId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    @Override
    public List<ActOfDelineationRenewalAbdAddressDecisionEntity> findAllByRenewalId(UUID renewalId) {
        return decisionRepository.findAllByActOfDelineationRenewalId(renewalId);
    }

    @Override
    public List<ActOfDelineationRenewalAbdAddressDecisionDto> getAllByRenewalId(UUID renewalId) {
        return decisionMapper.toDtoList(findAllByRenewalId(renewalId));
    }

    @Override
    public ActOfDelineationRenewalAbdAddressDecisionDto getById(UUID decisionId) {
        return decisionMapper.toDto(findById(decisionId));
    }

    // ─── HELPERS ───────────────────────────────────────────────────────────

    private void ensureStatus(ActOfDelineationRenewalAbdAddressDecisionEntity decision,
                              AbdAddressDecisionStatus... allowed) {
        Set<String> allowedCodes = Arrays.stream(allowed)
                .map(AbdAddressDecisionStatus::getCode)
                .collect(Collectors.toSet());
        if (!allowedCodes.contains(decision.getStatusCode())) {
            log.error("AOD RENEWAL DECISION [INVALID STATUS]: id=[{}], status=[{}], allowed=[{}]",
                    decision.getId(), decision.getStatusCode(), allowedCodes);
            throw new BusinessException(ErrorCode.INVALID_STATUS.name());
        }
    }

    private void ensureExecutor(ActOfDelineationRenewalAbdAddressDecisionEntity decision) {
        var currentUser = currentUserApiService.getCurrentUser();
        if (decision.getAssignees() == null || !decision.getAssignees().contains(currentUser.getId())) {
            log.error("AOD RENEWAL DECISION [ACCESS DENIED]: decisionId=[{}], userId=[{}], assignees=[{}]",
                    decision.getId(), currentUser.getId(), decision.getAssignees());
            throw new BusinessException(ErrorCode.ACCESS_DENIED.name());
        }
    }

    private void addRelatedUser(ActOfDelineationRenewalAbdAddressDecisionEntity decision, UUID userId) {
        Set<UUID> related = new HashSet<>(
                Optional.ofNullable(decision.getRelatedUsers()).orElse(new ArrayList<>()));
        related.add(userId);
        decision.setRelatedUsers(new ArrayList<>(related));
    }

    private void addRelatedUsers(ActOfDelineationRenewalAbdAddressDecisionEntity decision, Collection<UUID> userIds) {
        Set<UUID> related = new HashSet<>(
                Optional.ofNullable(decision.getRelatedUsers()).orElse(new ArrayList<>()));
        related.addAll(userIds);
        decision.setRelatedUsers(new ArrayList<>(related));
    }

    private AbdAddressEntity findAddressById(UUID abdAddressId) {
        return abdAddressRepository.findById(abdAddressId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    private ActOfDelineationRenewalEntity findRenewalById(UUID renewalId) {
        return renewalRepository.findById(renewalId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    private void updateRegistryData(ActOfDelineationRenewalEntity entity, Event event) {
        var dto = (ActOfDelineationRenewalDto) registryApiService.getByStatementId(entity.getStatementId());
        dto = renewalMapper.toStatementDto(dto, entity);
        dto.setEvent(event);
        registryApiService.updateData(dto.getId(), dto);
    }
}
