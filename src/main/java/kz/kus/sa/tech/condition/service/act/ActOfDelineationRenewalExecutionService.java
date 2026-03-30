package kz.kus.sa.tech.condition.service.act;

import kz.kus.sa.auth.api.currentuser.CurrentUserApiService;
import kz.kus.sa.auth.api.currentuser.dto.CurrentUserResponse;
import kz.kus.sa.auth.api.provider.ProviderApiService;
import kz.kus.sa.auth.api.user.UserApiService;
import kz.kus.sa.auth.api.user.dto.UserDto;
import kz.kus.sa.auth.api.user.dto.UserFilterDto;
import kz.kus.sa.registry.api.RegistryApiService;
import kz.kus.sa.registry.api.RegistrySignApiService;
import kz.kus.sa.registry.dto.common.AssignDto;
import kz.kus.sa.registry.dto.common.SignCreateDto;
import kz.kus.sa.registry.dto.renewal.ActOfDelineationRenewalDto;
import kz.kus.sa.registry.enums.Event;
import kz.kus.sa.registry.enums.Source;
import kz.kus.sa.report.api.ReportActOfDelineationRenewalApiService;
import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationRenewalEntity;
import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationRenewalExecutionEntity;
import kz.kus.sa.tech.condition.dao.mapper.AbdAddressMapper;
import kz.kus.sa.tech.condition.dao.mapper.ActOfDelineationRenewalMapper;
import kz.kus.sa.tech.condition.dao.mapper.ExternalSubdivisionMapper;
import kz.kus.sa.tech.condition.dao.mapper.ExternalUserMapper;
import kz.kus.sa.tech.condition.dao.repository.ActOfDelineationRenewalExecutionRepository;
import kz.kus.sa.tech.condition.dao.repository.ActOfDelineationRenewalRepository;
import kz.kus.sa.tech.condition.exception.BadRequestException;
import kz.kus.sa.tech.condition.exception.ErrorCode;
import kz.kus.sa.tech.condition.exception.NotFoundException;
import kz.kus.sa.tech.condition.service.address.AbdAddressService;
import kz.kus.sa.tech.condition.service.report.ActOfDelineationReportService;
import kz.kus.sa.tech.condition.statemachine.ActOfDelineationRenewalStatemachine;
import kz.kus.sa.tech.condition.statemachine.exception.GuardException;
import kz.kus.sa.tech.condition.statemachine.exception.UnknownEventException;
import kz.kus.sa.tech.condition.statemachine.exception.UnknownStateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.hibernate.internal.util.collections.CollectionHelper.isEmpty;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ActOfDelineationRenewalExecutionService {

    private final UserApiService userApiService;
    private final AbdAddressMapper abdAddressMapper;
    private final AbdAddressService abdAddressService;
    private final ProviderApiService providerApiService;
    private final ExternalUserMapper externalUserMapper;
    private final RegistryApiService registryApiService;
    private final CurrentUserApiService currentUserApiService;
    private final RegistrySignApiService registrySignApiService;
    private final ExternalSubdivisionMapper externalSubdivisionMapper;
    private final ActOfDelineationReportService actOfDelineationReportService;
    private final ActOfDelineationRenewalMapper actOfDelineationRenewalMapper;
    private final ActOfDelineationRenewalRepository actOfDelineationRenewalRepository;
    private final ActOfDelineationRenewalStatemachine actOfDelineationRenewalStatemachine;
    private final ReportActOfDelineationRenewalApiService reportActOfDelineationRenewalApiService;
    private final ActOfDelineationRenewalExecutionRepository actOfDelineationRenewalExecutionRepository;

    public ActOfDelineationRenewalExecutionEntity create(UUID actOfDelineationRenewalId, AssignDto assignDto) {
        var renewalEntity = actOfDelineationRenewalRepository.findById(actOfDelineationRenewalId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));

        ActOfDelineationRenewalExecutionEntity entity = new ActOfDelineationRenewalExecutionEntity();
        entity.setActOfDelineationRenewal(renewalEntity);

        List<UUID> assignees = new ArrayList<>();
//        Set<String> notificationRecipients = new HashSet<>();

        if (Event.ASSIGN_TO_DIVISION == assignDto.getEvent()) {
            if (isEmpty(assignDto.getDivisions())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            }
            List<UserDto> users = userApiService.searchAsList(UserFilterDto.builder()
                    .subDivisionIds(assignDto.getDivisions())
                    .permissions(List.of(Event.TCA_TAKE_TO_EXECUTION.name()))
                    .build());
            assignees.addAll(
                    users.stream()
                            .map(UserDto::getId)
                            .collect(Collectors.toList())
            );
//            notificationRecipients.addAll(
//                    users.stream()
//                            .map(UserDto::getEmail)
//                            .collect(Collectors.toSet())
//            );
        }
        if (Event.ASSIGN_TO_EXECUTOR == assignDto.getEvent()) {
            if (isEmpty(assignDto.getExecutors())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            }
            assignees.addAll(assignDto.getExecutors());
//            UserDto userDto = userApiService.getUserById(assignDto.getExecutors().get(0));

//            notificationRecipients.add(userDto.getEmail());
        }
        if (Event.ASSIGN_TO_DIVISION_WITH_ADDRESS == assignDto.getEvent()) {
            if (isEmpty(assignDto.getAddressDivisions())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            }

//            ActOfDelineationRenewalExecutionEntity finalEntity = entity;
            assignDto.getAddressDivisions().forEach(d -> {

                List<UserDto> users = userApiService.searchAsList(UserFilterDto.builder()
                        .subDivisionIds(List.of(d.getDivision()))
                        .permissions(List.of(Event.TCA_TAKE_TO_EXECUTION.name()))
                        .build());
                assignees.addAll(
                        users.stream()
                                .map(UserDto::getId)
                                .collect(Collectors.toList())
                );
//                notificationRecipients.addAll(
//                        users.stream()
//                                .map(UserDto::getEmail)
//                                .collect(Collectors.toSet())
//                );
            });
        }
        if (Event.ASSIGN_TO_EXECUTOR_WITH_ADDRESS == assignDto.getEvent()) {
            if (isEmpty(assignDto.getAddressExecutors())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            }
            assignDto.getAddressExecutors().forEach(e -> {
                assignees.add(e.getExecutor());
//                UserDto user = userApiService.getUserById(e.getExecutor());
//                notificationRecipients.add(user.getEmail());
            });
        }

        if (assignees.isEmpty()) {
            log.error("ERROR ACT OF DELINEATION RENEWAL [EXECUTION CREATING]: id = [{}], error = [{}]", entity.getId(), ErrorCode.NO_USERS_TO_ASSIGN.name());
            throw new BadRequestException(ErrorCode.NO_USERS_TO_ASSIGN.name());
        }

        entity.setAssignees(assignees);
        entity.setRelatedUsers(assignees);

        if (Source.MR.equals(renewalEntity.getSource())) {
            CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
            entity.setInitiator(externalUserMapper.fromCurrentUserResponse(currentUser));
        }

        entity = baseSave(entity);

        if (Event.ASSIGN_TO_DIVISION_WITH_ADDRESS == assignDto.getEvent()) {
            ActOfDelineationRenewalExecutionEntity finalEntity = entity;
            assignDto.getAddressDivisions().forEach(d ->
                    abdAddressService.saveList(null, abdAddressMapper.toEntityList(d.getObjectAbdAddresses()), finalEntity));
        } else if (Event.ASSIGN_TO_EXECUTOR_WITH_ADDRESS == assignDto.getEvent()) {
            ActOfDelineationRenewalExecutionEntity finalEntity = entity;
            assignDto.getAddressExecutors().forEach(e ->
                    abdAddressService.saveList(null, abdAddressMapper.toEntityList(e.getObjectAbdAddresses()), finalEntity));
        } else {
            abdAddressService.saveList(null, abdAddressService.getByActOfDelineationRenewalId(renewalEntity.getId()), entity);
        }

        log.info("ACT OF DELINEATION RENEWAL [EXECUTION CREATED]: id = [{}], execution id = [{}], executor = [{}], division = [{}]",
                renewalEntity.getId(), entity.getId(), assignDto.getExecutors(), assignDto.getDivisions());

        return entity;
    }

    public void takeToExecution(UUID id) {
        var entity = this.findById(id);
        var renewalEntity = entity.getActOfDelineationRenewal();

        checkState(renewalEntity, entity, Event.TAKE_TO_EXECUTION);

        var currentUser = currentUserApiService.getCurrentUser();
        setUsersOnEntity(entity, Collections.singletonList(currentUser.getId()), true);
        entity.setAssignedExecutor(null);
        entity.setAssignedSubdivision(null);
        entity.setOwner(externalUserMapper.fromCurrentUserResponse(currentUser));

        changeState(renewalEntity, entity, Event.TAKE_TO_EXECUTION);

        if (isNull(renewalEntity.getExecutor())) {
            renewalEntity.setExecutor(externalUserMapper.fromCurrentUserResponse(currentUser));
            renewalEntity.setAssignees(Collections.singletonList(currentUser.getId()));
        }

        baseSave(renewalEntity);

        log.info("ACT OF DELINEATION RENEWAL EXECUTION [TAKE TO EXECUTION]: id = [{}], executor = [{}]", entity.getId(), currentUser.getId());
        baseSave(entity);

        updateData(renewalEntity, Event.TAKE_TO_EXECUTION);
    }

    public void providerSign(UUID id, SignCreateDto sign) {
        var entity = this.findById(id);
        var renewalEntity = entity.getActOfDelineationRenewal();

        checkState(renewalEntity, entity, Event.PROVIDER_SIGN);

        var currentUser = currentUserApiService.getCurrentUser();
        renewalEntity.setDirector(externalUserMapper.fromCurrentUserResponse(currentUser));
        renewalEntity.setDirectorApprovedDatetime(OffsetDateTime.now());

        var actReportData = actOfDelineationReportService.actReportData(entity.getId());
        sign.setBase64Data(reportActOfDelineationRenewalApiService.actOfDelineationReportBase64(entity.getId(), actReportData));
        registrySignApiService.addProviderSign(renewalEntity.getStatementId(), sign);

        changeState(renewalEntity, entity, Event.PROVIDER_SIGN);

        log.info("ACT OF DELINEATION RENEWAL EXECUTION [PROVIDER SIGN]: id = [{}]", entity.getId());
        baseSave(entity);

        updateData(renewalEntity, Event.PROVIDER_SIGN);
    }

    public void consumerSign(UUID id, SignCreateDto sign) {
        var entity = this.findById(id);
        var renewalEntity = entity.getActOfDelineationRenewal();

        checkState(renewalEntity, entity, Event.CONSUMER_SIGN);

        registrySignApiService.addConsumerSign(renewalEntity.getStatementId(), sign);

        changeState(renewalEntity, entity, Event.CONSUMER_SIGN);

        log.info("ACT OF DELINEATION RENEWAL EXECUTION [CONSUMER SIGN]: id = [{}]", entity.getId());
        baseSave(entity);

        updateData(renewalEntity, Event.CONSUMER_SIGN);
    }

    private ActOfDelineationRenewalExecutionEntity findById(UUID id) {
        return actOfDelineationRenewalExecutionRepository.findByIdAndDeletedDatetimeIsNull(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    private ActOfDelineationRenewalExecutionEntity baseSave(ActOfDelineationRenewalExecutionEntity entity) {
        OffsetDateTime now = OffsetDateTime.now();
        if (isNull(entity.getCreatedDatetime())) {
            entity.setCreatedDatetime(now);
        }
        entity.setLastModifiedDatetime(now);
        return actOfDelineationRenewalExecutionRepository.save(entity);
    }

    private ActOfDelineationRenewalEntity baseSave(ActOfDelineationRenewalEntity entity) {
        OffsetDateTime now = OffsetDateTime.now();
        if (isNull(entity.getCreatedDatetime())) {
            entity.setCreatedDatetime(now);
        }
        entity.setLastModifiedDatetime(now);
        return actOfDelineationRenewalRepository.save(entity);
    }

    private void setUsersOnEntity(ActOfDelineationRenewalExecutionEntity entity, List<UUID> userIds, boolean isExecutor) {
        Set<UUID> relatedUsers = new HashSet<>(Optional.ofNullable(entity.getRelatedUsers()).orElse(new ArrayList<>()));
        relatedUsers.addAll(userIds);

        var currentUser = currentUserApiService.getCurrentUser();
        if (isExecutor) {
            var userId = userIds.stream().findFirst().orElse(null);
            if (userId == null) {
                throw new BadRequestException("User cannot be null! UserIds: " + userIds);
            } else {
                var user = userApiService.getUserById(userId);
                entity.setExecutor(externalUserMapper.toEntity(user));
            }
        }
        else
            entity.setAssignedBy(currentUser.getId());
        entity.setAssignees(userIds);
        entity.setRelatedUsers(new ArrayList<>(relatedUsers));
        entity.setCurrentUserId(currentUser.getId());
    }

    private void updateData(ActOfDelineationRenewalEntity entity, Event event) {
        var dto = (ActOfDelineationRenewalDto) registryApiService.getByStatementId(entity.getStatementId());
        dto = actOfDelineationRenewalMapper.toStatementDto(dto, entity);
        dto.setEvent(event);
        log.info("ACT OF DELINEATION RENEWAL [UPDATE REGISTRY DATA]: event = [{}], dto = [{}],", event, dto);
        registryApiService.updateData(dto.getId(), dto);
    }

    private void checkState(ActOfDelineationRenewalEntity entity, ActOfDelineationRenewalExecutionEntity execution, Event event) {
        log.info("ACT OF DELINEATION RENEWAL [CHECK-STATE]: status = [{}], event = [{}]", entity.getStatusCode(), event);
        try {
            actOfDelineationRenewalStatemachine.checkState(entity, execution, entity.getStatusCode(), event);
        } catch (UnknownStateException | UnknownEventException | GuardException e) {
            log.error("ACT OF DELINEATION RENEWAL [CHECK-STATE]: error message = {}", e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }

    private void changeState(ActOfDelineationRenewalEntity entity, ActOfDelineationRenewalExecutionEntity execution, Event event) {
        log.info("ACT OF DELINEATION RENEWAL [CHANGE-STATE]: status = [{}], event = [{}]", entity.getStatusCode(), event);
        try {
            actOfDelineationRenewalStatemachine.changeState(entity, execution, entity.getStatusCode(), event);
        } catch (UnknownStateException | UnknownEventException | GuardException e) {
            log.error("ACT OF DELINEATION RENEWAL [CHANGE-STATE]: error message = {}", e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }

}
