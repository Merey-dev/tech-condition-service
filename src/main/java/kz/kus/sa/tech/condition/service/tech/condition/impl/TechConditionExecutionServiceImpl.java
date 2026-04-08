package kz.kus.sa.tech.condition.service.tech.condition.impl;

import kz.kus.sa.auth.api.currentuser.CurrentUserApiService;
import kz.kus.sa.auth.api.currentuser.dto.CurrentUserResponse;
import kz.kus.sa.auth.api.provider.ProviderApiService;
import kz.kus.sa.auth.api.provider.dto.ProviderDto;
import kz.kus.sa.auth.api.provider.dto.SubdivisionDto;
import kz.kus.sa.auth.api.user.UserApiService;
import kz.kus.sa.auth.api.user.dto.UserDto;
import kz.kus.sa.auth.api.user.dto.UserFilterDto;
import kz.kus.sa.consumer.api.ConsumerApiService;
import kz.kus.sa.registry.api.RegistryApiService;
import kz.kus.sa.registry.api.RegistryGenerateNumberApiService;
import kz.kus.sa.registry.api.RegistrySignApiService;
import kz.kus.sa.registry.dto.common.AbdAddressDto;
import kz.kus.sa.registry.dto.common.AssignDto;
import kz.kus.sa.registry.dto.tc.v1.TechConditionStatementDto;
import kz.kus.sa.registry.enums.AuthProviderSubDivisionRole;
import kz.kus.sa.registry.enums.Event;
import kz.kus.sa.registry.enums.Source;
import kz.kus.sa.registry.enums.Status;
import kz.kus.sa.report.api.ReportTechConditionApiService;
import kz.kus.sa.tech.condition.dao.entity.HistoryEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionAbdAddressDecisionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionEntity;
import kz.kus.sa.tech.condition.dao.mapper.*;
import kz.kus.sa.tech.condition.dao.repository.TechConditionExecutionAbdAddressDecisionRepository;
import kz.kus.sa.tech.condition.dao.repository.TechConditionExecutionRepository;
import kz.kus.sa.tech.condition.dao.repository.TechConditionRepository;
import kz.kus.sa.tech.condition.dto.ChangeAssigneeDto;
import kz.kus.sa.tech.condition.dto.execution.TechConditionExecutionDto;
import kz.kus.sa.tech.condition.enums.AbdAddressDecisionStatus;
import kz.kus.sa.tech.condition.enums.ExecutionStatus;
import kz.kus.sa.tech.condition.enums.TechConditionExecutionType;
import kz.kus.sa.tech.condition.exception.BadRequestException;
import kz.kus.sa.tech.condition.exception.BusinessException;
import kz.kus.sa.tech.condition.exception.ErrorCode;
import kz.kus.sa.tech.condition.exception.NotFoundException;
import kz.kus.sa.tech.condition.service.address.AbdAddressService;
import kz.kus.sa.tech.condition.service.history.HistoryService;
import kz.kus.sa.tech.condition.service.intagration.KzharyqTechConditionService;
import kz.kus.sa.tech.condition.service.notification.NotificationService;
import kz.kus.sa.tech.condition.service.report.TechConditionReportService;
import kz.kus.sa.tech.condition.service.tech.condition.TechConditionExecutionAbdAddressDecisionService;
import kz.kus.sa.tech.condition.service.tech.condition.TechConditionExecutionService;
import kz.kus.sa.tech.condition.service.tech.condition.TechConditionProjectService;
import kz.kus.sa.tech.condition.statemachine.TechConditionExecutionStatemachine;
import kz.kus.sa.tech.condition.statemachine.TechConditionStatemachine;
import kz.kus.sa.tech.condition.statemachine.exception.GuardException;
import kz.kus.sa.tech.condition.statemachine.exception.UnknownEventException;
import kz.kus.sa.tech.condition.statemachine.exception.UnknownStateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static kz.kus.sa.tech.condition.dao.specification.TechConditionExecutionSpecification.*;
import static kz.kus.sa.tech.condition.util.CommonUtils.*;
import static kz.kus.sa.tech.condition.util.CommonUtils.formattedDate;
import static kz.kus.sa.tech.condition.util.Messages.*;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.hibernate.internal.util.collections.CollectionHelper.isEmpty;
import static org.hibernate.internal.util.collections.CollectionHelper.isNotEmpty;
import static org.springframework.data.jpa.domain.Specification.where;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TechConditionExecutionServiceImpl implements TechConditionExecutionService {

    private final UserApiService userApiService;
    private final HistoryService historyService;
    private final AbdAddressMapper abdAddressMapper;
    private final AbdAddressService abdAddressService;
    private final ProviderApiService providerApiService;
    private final ExternalUserMapper externalUserMapper;
    private final ConsumerApiService consumerApiService;
    private final RegistryApiService registryApiService;
    private final NotificationService notificationService;
    private final TechConditionMapper techConditionMapper;
    private final CurrentUserApiService currentUserApiService;
    private final RegistrySignApiService registrySignApiService;
    private final TechConditionRepository techConditionRepository;
    private final ExternalSubdivisionMapper externalSubdivisionMapper;
    private final TechConditionStatemachine techConditionStatemachine;
    private final TechConditionReportService techConditionReportService;
    private final TechConditionProjectService techConditionProjectService;
    private final KzharyqTechConditionService kzharyqTechConditionService;
    private final TechConditionExecutionMapper techConditionExecutionMapper;
    private final ReportTechConditionApiService reportTechConditionApiService;
    private final TechConditionExecutionRepository techConditionExecutionRepository;
    private final RegistryGenerateNumberApiService registryGenerateNumberApiService;
    private final TechConditionExecutionStatemachine techConditionExecutionStatemachine;
    private final TechConditionExecutionAbdAddressDecisionService abdAddressDecisionService;
    private final TechConditionExecutionAbdAddressDecisionRepository abdAddressDecisionRepository;

    @Override
    public Page<TechConditionExecutionDto> getAllForAdmin(String searchText,
                                                          List<String> statuses,
                                                          List<String> statementStatuses,
                                                          LocalDate dateFrom,
                                                          LocalDate dateTo,
                                                          Source source,
                                                          UUID userId,
                                                          UUID providerId,
                                                          Pageable pageable) {
        return techConditionExecutionRepository.findAll(getSpecification(searchText, statuses, statementStatuses,
                        dateFrom, dateTo, source, userId, providerId, true), pageable)
                .map(techConditionExecutionMapper::toDto);
    }

    @Override
    public void changeAssignee(ChangeAssigneeDto dto) {
        UserDto userDto = userApiService.getUserById(dto.getUserId());
        for (UUID id : dto.getIds()) {
            TechConditionExecutionEntity entity = findById(id);
            TechConditionEntity techConditionEntity = entity.getTechCondition();

            // обновляем execution
            checkState(techConditionEntity, Event.CHANGE_ASSIGNEE);

            if (isNotEmpty(entity.getAssignees())) {
                if (entity.getAssignees().size() == 1 && nonNull(techConditionEntity.getExecutor())) {
                    if (entity.getAssignees().get(0).equals(techConditionEntity.getExecutor().getId())) {
                        if (List.of(Status.ON_EXECUTION.getCode(),
                                        Status.RETURNED_FOR_REVISION.getCode(),
                                        Status.SIGNED.getCode())
                                .contains(techConditionEntity.getStatusCode())) {
                            techConditionEntity.setExecutor(externalUserMapper.toEntity(userDto));
                        }
                    }
                }
                entity.setAssignees(List.of(dto.getUserId()));
                if (isNotEmpty(techConditionEntity.getAssignees())) {
                    techConditionEntity.setAssignees(List.of(dto.getUserId()));
                }

                Set<UUID> mainRelatedUsers = new HashSet<>(techConditionEntity.getRelatedUsers());
                mainRelatedUsers.add(dto.getUserId());
                techConditionEntity.setRelatedUsers(new ArrayList<>(mainRelatedUsers));
                baseSave(techConditionEntity);

                Set<UUID> executionRelatedUsers = new HashSet<>(entity.getRelatedUsers());
                executionRelatedUsers.add(dto.getUserId());
                entity.setRelatedUsers(new ArrayList<>(executionRelatedUsers));

                entity.setOwner(externalUserMapper.toEntity(userDto));

                changeState(techConditionEntity, Event.CHANGE_ASSIGNEE);

                log.info("TECH CONDITION [ADMIN CHANGED ASSIGNEE]: id = [{}], execution id = [{}], new assignee userId = [{}]",
                        techConditionEntity.getId(), entity.getId(), dto.getUserId());
                baseSave(entity);

                // обновляем decisions которые не в финальном статусе
                List<TechConditionExecutionAbdAddressDecisionEntity> decisions = abdAddressDecisionRepository.findAllByTechConditionExecutionId(id);
                decisions.stream()
                        .filter(d -> !d.getStatusCode().equals(AbdAddressDecisionStatus.SIGNED.getCode()))
                        .forEach(d -> {
                            d.setAssignees(List.of(dto.getUserId()));
                            Set<UUID> decisionRelatedUsers = new HashSet<>(
                                    Optional.ofNullable(d.getAssignees()).orElse(new ArrayList<>()));
                            decisionRelatedUsers.add(dto.getUserId());
                            d.setRelatedUsers(new ArrayList<>(decisionRelatedUsers));
                            abdAddressDecisionRepository.save(d);
                        });

                log.info("TECH CONDITION [ADMIN CHANGED ASSIGNEE]: id=[{}], executionId=[{}], userId=[{}]",
                        techConditionEntity.getId(), entity.getId(), dto.getUserId());

                notificationService.send(userDto.getEmail(), String.format(
                        NEW_ASSIGN, IS_NAME, TC_SERVICE_NAME,
                        stringOrEmpty(techConditionEntity.getStatementRegistrationNumber()),
                        formattedDate(techConditionEntity.getApplicationDatetime())));
            }
        }
    }

    @Override
    public Page<TechConditionExecutionDto> getAll(String searchText,
                                                  List<String> statuses,
                                                  List<String> statementStatuses,
                                                  LocalDate dateFrom,
                                                  LocalDate dateTo,
                                                  Source source,
                                                  UUID userId,
                                                  Pageable pageable) {
        return techConditionExecutionRepository.findAll(getSpecification(searchText, statuses, statementStatuses,
                        dateFrom, dateTo, source, userId, null, false), pageable)
                .map(techConditionExecutionMapper::toDto);
    }

    private Specification<TechConditionExecutionEntity> getSpecification(String searchText,
                                                                         List<String> statuses,
                                                                         List<String> statementStatuses,
                                                                         LocalDate dateFrom,
                                                                         LocalDate dateTo,
                                                                         Source source,
                                                                         UUID userId,
                                                                         UUID providerId,
                                                                         boolean isAdmin) {
        Specification<TechConditionExecutionEntity> specification = where(isNotDeleted()
                .and(bySearchText(searchText))
                .and(byStatuses(statuses))
                .and(byStatementStatuses(statementStatuses))
                .and(byStatementDatetimeGreaterThanOrEqualTo(dateFrom))
                .and(byStatementDatetimeLessThanOrEqualTo(dateTo))
                .and(bySource(source)));
        if (isAdmin) {
            specification.and(byUserIdForAdmin(userId))
                    .and(byProviderId(providerId));
        } else {
            specification.and(sourceNotCrm());

            CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
            if (nonNull(userId) && !currentUser.getPermissions().contains(Event.TC_SIGN.name())) {//todo SIGN
                specification.and(byUserId(userId, currentUser.getId()));
            }
        }
        return specification;
    }

    @Override
    public TechConditionExecutionDto getById(UUID id) {
        return techConditionExecutionMapper.toDto(findById(id));
    }

    @Override
    public Page<TechConditionExecutionDto> findAllByTechConditionId(UUID techConditionId, Pageable pageable) {
        return techConditionExecutionRepository.findAllByTechConditionIdAndDeletedDatetimeIsNullOrderByCreatedDatetime(techConditionId, pageable)
                .map(techConditionExecutionMapper::toDto);
    }

    @Override
    public List<TechConditionExecutionDto> findAllByTechConditionIdList(UUID techConditionId) {
        return techConditionExecutionMapper.toDtoList(findAllByTechConditionId(techConditionId));
    }

    @Override
    public TechConditionExecutionEntity create(UUID techConditionId, AssignDto assignDto,
                                               TechConditionExecutionType type, Boolean isParallel,
                                               List<AbdAddressDto> objectAbdAddresses) {
        TechConditionEntity techConditionEntity = techConditionRepository.findById(techConditionId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));

        TechConditionExecutionEntity entity = new TechConditionExecutionEntity();
        entity.setExecutionType(type);
        entity.setTechCondition(techConditionEntity);
        entity.setIsParallel(isParallel);

        List<UUID> assignees = new ArrayList<>();
        Set<String> notificationRecipients = new HashSet<>();

        if (Event.ASSIGN_TO_DIVISION == assignDto.getEvent()) {
            if (isEmpty(assignDto.getDivisions())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            }
            List<UserDto> users = userApiService.searchAsList(UserFilterDto.builder()
                    .subDivisionIds(assignDto.getDivisions())
                    .permissions(List.of(Event.TCE_TAKE_TO_EXECUTION.name()))// todo TAKE_TO_EXECUTION
                    .build());
            assignees.addAll(
                    users.stream()
                            .map(UserDto::getId)
                            .collect(Collectors.toList())
            );
            notificationRecipients.addAll(
                    users.stream()
                            .map(UserDto::getEmail)
                            .collect(Collectors.toSet())
            );

            SubdivisionDto subdivision = providerApiService.getSubdivision(assignDto.getDivisions().get(0));
            entity.setAssignedSubdivision(externalSubdivisionMapper.toEntity(subdivision));
        }
        if (Event.ASSIGN_TO_EXECUTOR == assignDto.getEvent()) {
            if (isEmpty(assignDto.getExecutors())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            }
            assignees.addAll(assignDto.getExecutors());
            UserDto userDto = userApiService.getUserById(assignDto.getExecutors().get(0));
            entity.setAssignedExecutor(externalUserMapper.toEntity(userDto));
            entity.setOwner(externalUserMapper.toEntity(userDto));

            notificationRecipients.add(userDto.getEmail());
        }
        if (Event.ASSIGN_TO_DIVISION_WITH_ADDRESS == assignDto.getEvent()) {
            if (isEmpty(assignDto.getAddressDivisions())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            }
//            List<SubdivisionDto> subdivisionList = new ArrayList<>();

//            TechConditionExecutionEntity finalEntity = entity;
            assignDto.getAddressDivisions().forEach(d -> {
//                subdivisionList.add(providerApiService.getSubdivision(d.getDivision()));

                List<UserDto> users = userApiService.searchAsList(UserFilterDto.builder()
                        .subDivisionIds(List.of(d.getDivision()))
                        .permissions(List.of(Event.TCE_TAKE_TO_EXECUTION.name()))// todo TAKE_TO_EXECUTION
                        .build());
                assignees.addAll(
                        users.stream()
                                .map(UserDto::getId)
                                .collect(Collectors.toList())
                );
                notificationRecipients.addAll(
                        users.stream()
                                .map(UserDto::getEmail)
                                .collect(Collectors.toSet())
                );
            });
//            entity.setAssignedSubdivision(externalSubdivisionMapper.toEntity(subdivisionList.get(0)));//todo refactor List<>
        }
        if (Event.ASSIGN_TO_EXECUTOR_WITH_ADDRESS == assignDto.getEvent()) {
            if (isEmpty(assignDto.getAddressExecutors())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            }
//            List<UserDto> userList = new ArrayList<>();

            assignDto.getAddressExecutors().forEach(e -> {
                assignees.add(e.getExecutor());
                UserDto user = userApiService.getUserById(e.getExecutor());
//                userList.add(user);
                notificationRecipients.add(user.getEmail());
            });

//            entity.setAssignedExecutor(externalUserMapper.toEntity(userList.get(0)));//todo refactor List<>
        }

        if (assignees.isEmpty()) {
            log.error("ERROR TECH CONDITION [EXECUTION CREATING]: id = [{}], error = [{}]", entity.getId(), ErrorCode.NO_USERS_TO_ASSIGN.name());
            throw new BadRequestException(ErrorCode.NO_USERS_TO_ASSIGN.name());
        }

        entity.setAssignees(assignees);
        entity.setRelatedUsers(assignees);

        if (isFalse(isParallel)) {
            Set<UUID> newRelatedUsers = new HashSet<>(techConditionEntity.getRelatedUsers());
            newRelatedUsers.addAll(assignees);
            entity.setRelatedUsers(new ArrayList<>(newRelatedUsers));
        }

        if (Source.MR.equals(techConditionEntity.getSource())) {
            CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
            entity.setInitiator(externalUserMapper.fromCurrentUserResponse(currentUser));
        }

        entity = baseSave(entity);

        abdAddressService.saveList(null, abdAddressMapper.toEntityList(objectAbdAddresses), entity);

//        abdAddressDecisionService.replaceAll(entity.getId(), dto.getAddressDecisions());

        log.info("TECH CONDITION [EXECUTION CREATED]: id = [{}], execution id = [{}], parallel = [{}], executor = [{}], division = [{}]",
                techConditionEntity.getId(), entity.getId(), isParallel, assignDto.getExecutors(), assignDto.getDivisions());

        if (isParallel) {
            notificationService.send(mergeEmails(notificationRecipients), String.format(
                    NEW_ASSIGN, IS_NAME, TC_SERVICE_NAME,
                    stringOrEmpty(techConditionEntity.getStatementRegistrationNumber()),
                    formattedDate(techConditionEntity.getStatementRegistrationDatetime())));
        }

        return entity;
    }

    @Override
    public TechConditionExecutionEntity baseSave(TechConditionExecutionEntity entity) {
        OffsetDateTime now = OffsetDateTime.now();
        if (isNull(entity.getCreatedDatetime())) {
            entity.setCreatedDatetime(now);
        }
        entity.setLastModifiedDatetime(now);
        return techConditionExecutionRepository.save(entity);
    }

    private TechConditionEntity baseSave(TechConditionEntity entity) {
        entity.setLastModifiedDatetime(OffsetDateTime.now());
        return techConditionRepository.save(entity);
    }

    private TechConditionExecutionEntity findById(UUID id) {
        return techConditionExecutionRepository.findByIdAndDeletedDatetimeIsNull(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    @Override
    public List<TechConditionExecutionEntity> findAllByTechConditionId(UUID techConditionId) {
        return techConditionExecutionRepository.findAllByTechConditionIdAndDeletedDatetimeIsNullOrderByCreatedDatetime(techConditionId);
    }

    /** EVENT */
    @Override
    public void takeToExecution(UUID id, AssignDto dto) {
        TechConditionExecutionEntity entity = findById(id);
        TechConditionEntity techConditionEntity = entity.getTechCondition();

        executionCheckState(techConditionEntity, entity, dto.getEvent());

        CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
        if (entity.getAssignees().size() > 1) {
            entity.setAssignees(List.of(currentUser.getId()));
        }
        if (techConditionEntity.getAssignees().size() > 1) {
            techConditionEntity.setAssignees(List.of(currentUser.getId()));
        }
        entity.setAssignedExecutor(null);
        entity.setAssignedSubdivision(null);

        boolean statusChanged = false;
        if (Event.TCE_TAKE_TO_EXECUTION == dto.getEvent()) {
            statusChanged = !Objects.equals(techConditionEntity.getStatusCode(), Status.ON_EXECUTION.getCode());
            if (Objects.equals(techConditionEntity.getStatusCode(), Status.ASSIGNED.getCode())) {
                techConditionEntity.setStatusCode(Status.ON_EXECUTION.getCode());
            }
        } else {
            throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
        }
        entity.setOwner(externalUserMapper.fromCurrentUserResponse(currentUser));

        executionChangeState(techConditionEntity, entity, dto.getEvent());

        if (isNull(techConditionEntity.getExecutor())) {
            techConditionEntity.setExecutor(externalUserMapper.fromCurrentUserResponse(currentUser));
            techConditionEntity.setAssignees(List.of(currentUser.getId()));
        }

        baseSave(techConditionEntity);

        // todo DP
//        if (Objects.equals(techConditionEntity.getStatusCode(), Status.ON_EXECUTION.getCode()) && statusChanged) {
//            try {
//                techConditionKafkaService.createDpResponseAndSendToKafka(techConditionEntity);// todo DP
//            } catch (Exception e) {
//                log.error("ERROR SENDING TO DP REGISTERED TECH CONDITION: id = [{}], registration number = [{}]",
//                        techConditionEntity.getStatementId(), techConditionEntity.getStatementRegistrationNumber(), e);
//            }
//        }

        log.info("TECH CONDITION [EXECUTION TAKEN TO EXECUTION]: id = [{}], execution id = [{}], executor = [{}]",
                techConditionEntity.getId(), entity.getId(), currentUser.getId());
        baseSave(entity);
    }

    @Override
    public void assign(UUID id, AssignDto dto) {
        TechConditionExecutionEntity entity = findById(id);
        TechConditionEntity techConditionEntity = entity.getTechCondition();

        executionCheckState(techConditionEntity, entity, dto.getEvent());

        // can't if executed
        if (Objects.equals(entity.getStatusCode(), ExecutionStatus.ON_EXECUTION.getCode())
                && nonNull(entity.getExecutor())) {
            throw new BusinessException(ErrorCode.NOT_ALLOWED.name());
        }
        List<UUID> assignees = new ArrayList<>();
        Set<String> notificationRecipients = new HashSet<>();

        if (Event.ASSIGN_TO_DIVISION == dto.getEvent()) {
            if (isEmpty(dto.getDivisions())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            } else {
                List<SubdivisionDto> subdivisions = new ArrayList<>();
                dto.getDivisions().forEach(subdivisionId ->
                        subdivisions.add(
                                providerApiService.getSubdivision(subdivisionId))
                );

                List<UserDto> users = userApiService.searchAsList(UserFilterDto.builder()
                        .subDivisionIds(dto.getDivisions())
                        .permissions(List.of(Event.TCE_TAKE_TO_EXECUTION.name())) //todo TAKE_TO_EXECUTION
                        .build());
                assignees.addAll(
                        users.stream()
                                .map(UserDto::getId)
                                .collect(Collectors.toList())
                );
                notificationRecipients.addAll(
                        users.stream()
                                .map(UserDto::getEmail)
                                .collect(Collectors.toSet())
                );

                entity.setAssignedSubdivision(externalSubdivisionMapper.toEntity(subdivisions.get(0)));//todo refactor List<>
                entity.setOwner(externalUserMapper.toEntity(users.get(0)));//todo refactor List<>
            }
        }
        if (Event.ASSIGN_TO_EXECUTOR == dto.getEvent()) {
            if (isEmpty(dto.getExecutors())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            } else {
                assignees.addAll(dto.getExecutors());

                dto.getExecutors().forEach(userId ->
                        notificationRecipients.add(
                                userApiService.getUserById(userId).getEmail())
                );

                UserDto userDto = userApiService.getUserById(dto.getExecutors().get(0));//todo refactor List<>
                SubdivisionDto subdivisionDto = providerApiService.getSubdivision(userDto.getSubdivisionId());
                entity.setAssignedExecutor(externalUserMapper.toEntity(userDto));
                entity.setAssignedSubdivision(externalSubdivisionMapper.toEntity(subdivisionDto));
                entity.setOwner(externalUserMapper.toEntity(userDto));
            }
        }
        if (Event.ASSIGN_TO_DIVISION_WITH_ADDRESS == dto.getEvent()
                || Event.ASSIGN_TO_EXECUTOR_WITH_ADDRESS == dto.getEvent()) {
            abdAddressDecisionService.assign(id, dto);
            // assignees для execution собираем из decisions
            List<TechConditionExecutionAbdAddressDecisionEntity> decisions =
                    abdAddressDecisionService.findAllByExecutionId(id);
            assignees.addAll(
                    decisions.stream()
                            .flatMap(d -> d.getAssignees().stream())
                            .distinct()
                            .collect(Collectors.toList())
            );
        }

        if (assignees.isEmpty()) {
            log.error("ERROR TECH CONDITION [EXECUTION ASSIGNING]: id = [{}], error = [{}]", techConditionEntity.getId(), ErrorCode.NO_USERS_TO_ASSIGN.name());
            throw new BadRequestException(ErrorCode.NO_USERS_TO_ASSIGN.name());
        }

        Set<UUID> executionRelatedUsers = new HashSet<>(entity.getRelatedUsers());
        executionRelatedUsers.addAll(assignees);
        entity.setRelatedUsers(new ArrayList<>(executionRelatedUsers));

        Set<UUID> relatedUsers = new HashSet<>(techConditionEntity.getRelatedUsers());
        relatedUsers.addAll(assignees);
        techConditionEntity.setRelatedUsers(new ArrayList<>(relatedUsers));
        baseSave(techConditionEntity);

        CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
        entity.setAssignedBy(currentUser.getId());
        entity.setRevisionReason(dto.getComment());

        executionChangeState(techConditionEntity, entity, dto.getEvent());

        entity.setAssignees(new ArrayList<>(assignees));

        log.info("TECH CONDITION [EXECUTION ASSIGNED]: id = [{}], execution id = [{}], type = [{}], executor = [{}], division = [{}]",
                techConditionEntity.getId(), entity.getId(), dto.getEvent(), dto.getExecutors(), dto.getDivisions());
        baseSave(entity);

        notificationService.send(mergeEmails(notificationRecipients), String.format(
                NEW_ASSIGN, IS_NAME, TC_SERVICE_NAME,
                stringOrEmpty(techConditionEntity.getStatementRegistrationNumber()),
                formattedDate(techConditionEntity.getApplicationDatetime())));
    }

    /*@Deprecated
    @Override
    public void reAssign(UUID id, AssignDto dto) {
        TechConditionExecutionEntity entity = findById(id);
        TechConditionEntity techConditionEntity = entity.getTechCondition();

        executionCheckState(techConditionEntity, entity, dto.getEvent());

        List<UUID> assignees = new ArrayList<>();
        Set<String> notificationRecipients = new HashSet<>();

        if (Event.ASSIGN_TO_DIVISION == dto.getEvent()) {
            if (isEmpty(dto.getDivisions())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            } else {
                List<SubdivisionDto> subdivisions = new ArrayList<>();
                dto.getDivisions().forEach(subdivisionId ->
                        subdivisions.add(
                                providerApiService.getSubdivision(subdivisionId))
                );

                List<UserDto> users = userApiService.searchAsList(UserFilterDto.builder()
                        .subDivisionIds(dto.getDivisions())
                        .permissions(List.of(Event.TCE_TAKE_TO_EXECUTION.name())) //todo TAKE_TO_EXECUTION
                        .build());
                assignees.addAll(
                        users.stream()
                                .map(UserDto::getId)
                                .collect(Collectors.toList())
                );
                notificationRecipients.addAll(
                        users.stream()
                                .map(UserDto::getEmail)
                                .collect(Collectors.toSet())
                );

                entity.setAssignedSubdivision(externalSubdivisionMapper.toEntity(subdivisions.get(0)));//todo refactor List<>
                entity.setOwner(externalUserMapper.toEntity(users.get(0)));//todo refactor List<>
            }
        }
        if (Event.ASSIGN_TO_EXECUTOR == dto.getEvent()) {
            if (isEmpty(dto.getExecutors())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            } else {
                assignees.addAll(dto.getExecutors());

                dto.getExecutors().forEach(userId ->
                        notificationRecipients.add(
                                userApiService.getUserById(userId).getEmail())
                );

                UserDto userDto = userApiService.getUserById(dto.getExecutors().get(0));//todo refactor List<>
                SubdivisionDto subdivisionDto = providerApiService.getSubdivision(userDto.getSubdivisionId());
                entity.setAssignedExecutor(externalUserMapper.toEntity(userDto));
                entity.setAssignedSubdivision(externalSubdivisionMapper.toEntity(subdivisionDto));
                entity.setOwner(externalUserMapper.toEntity(userDto));
            }
        }
        if (Event.ASSIGN_TO_DIVISION_WITH_ADDRESS == dto.getEvent()) {
            if (isEmpty(dto.getAddressDivisions())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            } else {
                List<SubdivisionDto> subdivisionList = new ArrayList<>();
                List<UserDto> userList = new ArrayList<>();

                dto.getAddressDivisions().forEach(d -> {
                    subdivisionList.add(providerApiService.getSubdivision(d.getDivision()));

                    List<UserDto> users = userApiService.searchAsList(UserFilterDto.builder()
                            .subDivisionIds(List.of(d.getDivision()))
                            .permissions(List.of(Event.TCE_TAKE_TO_EXECUTION.name())) //todo TAKE_TO_EXECUTION
                            .build());
                    userList.addAll(users);
                    assignees.addAll(
                            users.stream()
                                    .map(UserDto::getId)
                                    .collect(Collectors.toList())
                    );
                    notificationRecipients.addAll(
                            users.stream()
                                    .map(UserDto::getEmail)
                                    .collect(Collectors.toSet())
                    );
                });
                entity.setAssignedSubdivision(externalSubdivisionMapper.toEntity(subdivisionList.get(0)));//todo refactor List<>
                entity.setOwner(externalUserMapper.toEntity(userList.get(0)));//todo refactor List<>
            }
        }
        if (Event.ASSIGN_TO_EXECUTOR_WITH_ADDRESS == dto.getEvent()) {
            if (isEmpty(dto.getAddressExecutors())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            } else {
                List<UserDto> userList = new ArrayList<>();

                dto.getAddressExecutors().forEach(e -> {
                    userList.add(userApiService.getUserById(e.getExecutor()));
                    assignees.add(e.getExecutor());
                });
                notificationRecipients.add(userList.get(0).getEmail());

                SubdivisionDto subdivisionDto = providerApiService.getSubdivision(userList.get(0).getSubdivisionId());//todo refactor List<>
                entity.setAssignedExecutor(externalUserMapper.toEntity(userList.get(0)));//todo refactor List<>
                entity.setAssignedSubdivision(externalSubdivisionMapper.toEntity(subdivisionDto));
                entity.setOwner(externalUserMapper.toEntity(userList.get(0)));//todo refactor List<>
            }
        }

        if (assignees.isEmpty()) {
            log.error("ERROR TECH CONDITION [EXECUTION RE-ASSIGNING]: id = [{}], error = [{}]", techConditionEntity.getId(), ErrorCode.NO_USERS_TO_ASSIGN.name());
            throw new BadRequestException(ErrorCode.NO_USERS_TO_ASSIGN.name());
        }

        Set<UUID> executionRelatedUsers = new HashSet<>(entity.getRelatedUsers());
        executionRelatedUsers.addAll(assignees);
        entity.setRelatedUsers(new ArrayList<>(executionRelatedUsers));

        Set<UUID> relatedUsers = new HashSet<>(techConditionEntity.getRelatedUsers());
        relatedUsers.addAll(assignees);
        techConditionEntity.setRelatedUsers(new ArrayList<>(relatedUsers));
        baseSave(techConditionEntity);

        CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
        entity.setAssignedBy(currentUser.getId());
        entity.setRevisionReason(dto.getComment());

        executionChangeState(techConditionEntity, entity, dto.getEvent());

        entity.setAssignees(new ArrayList<>(assignees));

        log.info("TECH CONDITION [EXECUTION RE-ASSIGNED]: id = [{}], execution id = [{}], type = [{}], executor = [{}], division = [{}]",
                techConditionEntity.getId(), entity.getId(), dto.getEvent(), dto.getExecutors(), dto.getDivisions());
        baseSave(entity);

        notificationService.send(mergeEmails(notificationRecipients), String.format(
                NEW_ASSIGN, IS_NAME, TC_SERVICE_NAME,
                stringOrEmpty(techConditionEntity.getStatementRegistrationNumber()),
                formattedDate(techConditionEntity.getApplicationDatetime())));
    }

    @Override
    public void assignForApproval(UUID id, AssignDto dto) {
        TechConditionExecutionEntity entity = findById(id);
        TechConditionEntity techConditionEntity = entity.getTechCondition();

        if (!Objects.equals(Event.TCE_SEND_FOR_APPROVAL, dto.getEvent())) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
        }

        if (isNull(dto.getExecutors())) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
        }

        executionCheckState(techConditionEntity, entity, dto.getEvent());

        List<UUID> assignees = dto.getExecutors();

        StringJoiner owners = new StringJoiner(", ");
        for (UUID userId : assignees) {
            UserDto userDto = userApiService.getUserById(userId);
            owners.add(userDto.getFullName());
        }
        entity.setOwner(null);
//        entity.setOwnerFullName(owners.toString()); todo

        executionChangeState(techConditionEntity, entity, dto.getEvent());

        entity.setAssignees(new ArrayList<>(assignees));

        Set<UUID> executionRelatedUsers = new HashSet<>(entity.getRelatedUsers());
        executionRelatedUsers.addAll(assignees);
        entity.setRelatedUsers(new ArrayList<>(executionRelatedUsers));

        Set<UUID> relatedUsers = new HashSet<>(techConditionEntity.getRelatedUsers());
        relatedUsers.addAll(assignees);
        techConditionEntity.setRelatedUsers(new ArrayList<>(relatedUsers));
        baseSave(techConditionEntity);

        log.info("TECH CONDITION [EXECUTION SENT FOR APPROVE]: id = [{}], execution id = [{}], to user id = [{}]",
                techConditionEntity.getId(), entity.getId(), dto.getExecutors());
        baseSave(entity);

        // todo updateData

        Set<String> notificationEmails = new HashSet<>();
        dto.getExecutors().forEach(user -> notificationEmails.add(userApiService.getUserById(user).getEmail()));

        notificationService.send(mergeEmails(notificationEmails), String.format(
                NEW_APPROVAL, IS_NAME, TC_SERVICE_NAME,
                stringOrEmpty(techConditionEntity.getStatementRegistrationNumber()),
                formattedDate(techConditionEntity.getApplicationDatetime())));
    }

    @Override
    public void sendForRevision(UUID id, String reason) {
        TechConditionExecutionEntity entity = findById(id);
        TechConditionEntity techConditionEntity = entity.getTechCondition();

        if (StringUtils.isEmpty(reason)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.name());
        }

        UUID returnToUserId = null;

        if (Objects.equals(techConditionEntity.getStatusCode(), Status.ON_EXECUTION.getCode())) {
            executionCheckState(techConditionEntity, entity, Event.TCE_SEND_FOR_REVISION);
            entity.setRevisionReason(reason);

            executionChangeState(techConditionEntity, entity, Event.TCE_SEND_FOR_REVISION);
            returnToUserId = entity.getExecutor().getId();
        } else {
            checkState(techConditionEntity, Event.TCE_SEND_FOR_REVISION);
//            techConditionEntity.setRevisionReason(reason); todo
            changeState(techConditionEntity, Event.TCE_SEND_FOR_REVISION);
            if (Objects.equals(techConditionEntity.getStatusCode(), Status.RETURNED_FOR_REVISION.getCode())) {
                returnToUserId = nonNull(techConditionEntity.getManager())
                        ? techConditionEntity.getManager().getId()
                        : techConditionEntity.getExecutor().getId();
                techConditionEntity.setAssignees(List.of(returnToUserId));
            }
        }
        if (isNull(returnToUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.name());
        }
        entity.setAssignees(List.of(returnToUserId));
        baseSave(techConditionEntity);

        UserDto userDto = userApiService.getUserById(returnToUserId);
        entity.setOwner(externalUserMapper.toEntity(userDto));

        log.info("TECH CONDITION [EXECUTION SENT FOR REVISION]: id = [{}], execution id = [{}], return to user id = [{}]",
                techConditionEntity.getId(), entity.getId(), returnToUserId);
        baseSave(entity);

        notificationService.send(userDto.getEmail(), String.format(
                NEW_REVISION, IS_NAME,
                stringOrEmpty(techConditionEntity.getStatementRegistrationNumber()),
                formattedDate(techConditionEntity.getApplicationDatetime())));
    }

    @Override
    public void approve(UUID id) {
        TechConditionExecutionEntity entity = findById(id);
        TechConditionEntity techConditionEntity = entity.getTechCondition();

        executionCheckState(techConditionEntity, entity, Event.TCE_APPROVE);

        CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
        entity.setManager(externalUserMapper.fromCurrentUserResponse(currentUser));
        entity.setManagerApprovedDatetime(OffsetDateTime.now());

//        if (entity.getDecisionType() == REASONED_REFUSAL) { //todo
//            techConditionEntity.setDecisionType(REASONED_REFUSAL);
//        } else if (entity.getDecisionType() == TECHNICAL_RECOMMENDATION
//                && (isNull(techConditionEntity.getDecisionType()))) {
//            techConditionEntity.setDecisionType(TECHNICAL_RECOMMENDATION);
//        }
        baseSave(techConditionEntity);

        executionChangeState(techConditionEntity, entity, Event.TCE_APPROVE);

        entity.setAssignees(new ArrayList<>());
        baseSave(entity);

        log.info("TECH CONDITION [EXECUTION APPROVED]: id = [{}], execution id = [{}]", techConditionEntity.getId(), entity.getId());

        isAllExecutionsApproved(techConditionEntity);//todo ПТС
    }

    private void isAllExecutionsApproved(TechConditionEntity techConditionEntity) {
        boolean allExecutionsIsDone = !haveNotCompletedByTechConditionId(techConditionEntity.getId(), ExecutionStatus.APPROVED);
        if (allExecutionsIsDone) {
            // todo return to TC_EXECUTOR division users
            ProviderDto providerDto = providerApiService.getProviderDto(techConditionEntity.getProviderId());

            List<SubdivisionDto> subdivisions = Optional.ofNullable(
                            providerApiService.getSubdivisionsByBinAndRole(providerDto.getIinBin(),
                                    AuthProviderSubDivisionRole.TC_EXECUTOR.toString()))
                    .filter(list -> !list.isEmpty())
                    .orElseThrow(() -> new NotFoundException("No subdivisions found with role = " + AuthProviderSubDivisionRole.TC_EXECUTOR));
            List<UUID> subdivisionIds = subdivisions.stream()
                    .map(SubdivisionDto::getId)
                    .collect(Collectors.toList());

            List<UserDto> userList = Optional.ofNullable(
                            userApiService.searchAsList(UserFilterDto.builder()
                                    .subDivisionIds(subdivisionIds)
                                    .permissions(List.of(Event.TCE_TAKE_TO_EXECUTION.name()))
                                    .build()))
                    .filter(list -> !list.isEmpty())
                    .orElseThrow(() -> new NotFoundException("No users found with permission = " + Event.TCE_TAKE_TO_EXECUTION.name()));
            List<UUID> assignees = userList.stream()
                    .map(UserDto::getId)
                    .collect(Collectors.toList());

            techConditionEntity.setAssignees(assignees);
            techConditionEntity.setHasActiveExecutions(false);

            TechConditionExecutionEntity mainExecution = findAllByTechConditionId(techConditionEntity.getId())
                    .stream()
                    .filter(e -> !e.getIsParallel())
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));
            mainExecution.setAssignees(assignees);
            mainExecution.setOwner(techConditionEntity.getExecutor());

            techConditionEntity.setExecutor(null);
            baseSave(techConditionEntity);
            log.info("TECH CONDITION [RETURNED TO TC_EXECUTOR DIVISION USERS]: id = [{}], execution id = [{}]",
                    techConditionEntity.getId(), mainExecution.getId());
            baseSave(mainExecution);
        }
    }

    private Boolean haveNotCompletedByTechConditionId(UUID id, ExecutionStatus status) {
        return techConditionExecutionRepository.existsByTechConditionIdAndDeletedDatetimeIsNullAndStatusCodeIsNot(id, status.getCode());
    }

    //todo refactor execution history
    @Override
    public void withdraw(UUID id) {
        TechConditionExecutionEntity entity = findById(id);
        TechConditionEntity techConditionEntity = entity.getTechCondition();

        if (!List.of(ExecutionStatus.ASSIGNED.getCode(), ExecutionStatus.UNDER_APPROVAL.getCode()).contains(entity.getStatusCode())) {
            throw new BusinessException(ErrorCode.NOT_ALLOWED.name());
        }
        CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
        if (!entity.getAssignedBy().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED.name());
        }
        List<HistoryEntity> lastHistory = historyService.getLast2ByTechConditionExecutionId(id);
        if (lastHistory.isEmpty() || lastHistory.size() < 2) {
            // first assign, can't withdraw only change assignees
            throw new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name());
        }

        executionCheckState(techConditionEntity, entity, Event.TCE_WITHDRAW);

        entity.setAssignees(List.of(lastHistory.get(1).getCurrentUser().getId()));
        entity.setStatusCode(lastHistory.get(1).getExecutionStatus());

        executionChangeState(techConditionEntity, entity, Event.TCE_WITHDRAW);

        baseSave(entity);
    }

    @Override
    public void assignForSign(UUID id, AssignDto dto) {
        TechConditionExecutionEntity entity = findById(id);
        TechConditionEntity techConditionEntity = entity.getTechCondition();

        if (Event.TC_SEND_FOR_SIGN != dto.getEvent()) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
        }
        if (isEmpty(dto.getExecutors())) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
        }

        executionCheckState(techConditionEntity, entity, dto.getEvent());

        List<UUID> assignees = new ArrayList<>(dto.getExecutors());
        Set<UUID> executionRelatedUsers = new HashSet<>(entity.getRelatedUsers());
        executionRelatedUsers.addAll(assignees);
        entity.setRelatedUsers(new ArrayList<>(executionRelatedUsers));

//        StringJoiner owners = new StringJoiner(", ");
//        for (UUID userId : assignees) {
//            UserDto userDto = userApiService.getUserById(userId);
//            owners.add(userDto.getFullName());
//        }
//        entity.setOwner(null); //todo owners.toString()

        executionChangeState(techConditionEntity, entity, dto.getEvent());

        entity.setAssignees(new ArrayList<>(assignees));

        Set<UUID> relatedUsers = new HashSet<>(techConditionEntity.getRelatedUsers());
        relatedUsers.addAll(assignees);
        techConditionEntity.setRelatedUsers(new ArrayList<>(relatedUsers));
        baseSave(techConditionEntity);

        log.info("TECH CONDITION [EXECUTION SENT FOR SIGN]: id = [{}], execution id = [{}], to user id = [{}]",
                techConditionEntity.getId(), entity.getId(), dto.getExecutors());
        baseSave(entity);
    }


    */
    /** DECISIONS */
    /*
    @Override
    public void assignParallel(UUID id, AssignDto dto) {
        TechConditionExecutionEntity entity = findById(id);
        TechConditionEntity techConditionEntity = entity.getTechCondition();

        if (dto.getEvent() != Event.TC_CREATE_PARALLEL_EXECUTION && isEmpty(dto.getDivisions())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.name());
        }

        changeState(techConditionEntity, Event.TC_CREATE_PARALLEL_EXECUTION);

        CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
//        StringJoiner owners = new StringJoiner(", ");
        dto.getDivisions().forEach(division -> {
            AssignDto assignDto = AssignDto.builder()
                    .event(Event.ASSIGN_TO_DIVISION)
                    .divisions(dto.getDivisions())
                    .build();
            TechConditionExecutionEntity execution = create(techConditionEntity.getId(),
                    assignDto, TechConditionExecutionType.APPLICATION, true, abdAddressMapper.toDtoList(entity.getObjectAbdAddresses()));

            execution.setAssignedBy(currentUser.getId());
//            owners.add(execution.getOwner().getDivision());
            baseSave(execution);
        });

        techConditionEntity.setAssignees(new ArrayList<>());
        techConditionEntity.setHasActiveExecutions(true);
        techConditionEntity.setHasParallelExecutions(true);

        entity.setAssignees(new ArrayList<>());
//        entity.setOwner(owners.toString());//todo
        baseSave(entity);

        log.info("TECH CONDITION [EXECUTION ASSIGNED PARALLEL]: id = [{}], execution id = [{}]", techConditionEntity.getId(), entity.getId());

        baseSave(techConditionEntity);
    }

    @Override
    public void sendDecisionForApproval(UUID id, UUID userId) {
        TechConditionExecutionEntity entity = findById(id);
        TechConditionEntity techConditionEntity = entity.getTechCondition();

        checkState(techConditionEntity, Event.TC_SEND_FOR_APPROVAL);

        UserDto userDto = userApiService.getUserById(userId);

//        boolean haveActiveExecutions = haveNotCompletedByTechConditionId(id);
//
//        if (haveActiveExecutions) {
//            List<TechConditionExecutionEntity> allActiveExecutions = findAllByTechConditionId(id)
//                    .stream()
//                    .filter(e -> !Objects.equals(e.getStatusCode(), ExecutionStatus.APPROVED.getCode()))
//                    .collect(Collectors.toList());
//            if (allActiveExecutions.size() == 1) {
//                TechConditionExecutionEntity execution = allActiveExecutions.get(0);
//                if (execution.getAssignees().contains(techConditionEntity.getExecutor().getId())) {
//                    techConditionEntity.setDecisionType(execution.getDecisionType()); // todo decision
//                }
//            } else throw new BusinessException(ErrorCode.NOT_ALLOWED.name());
//        }

        // for re-sending for approval
        SubdivisionDto subdivisionDto = providerApiService.getSubdivision(userDto.getSubdivisionId());
        entity.setAssignedSubdivision(externalSubdivisionMapper.toEntity(subdivisionDto));
        entity.setOwner(externalUserMapper.toEntity(userDto));

        Set<UUID> executionRelatedUsers = new HashSet<>(entity.getRelatedUsers());
        executionRelatedUsers.add(userId);
        entity.setRelatedUsers(new ArrayList<>(executionRelatedUsers));

        changeState(techConditionEntity, Event.TC_SEND_FOR_APPROVAL);

        techConditionEntity.setAssignees(List.of(userId));
        entity.setAssignees(List.of(userId));

        CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
        entity.setAssignedBy(currentUser.getId());

        baseSave(entity);

        log.info("TECH CONDITION [EXECUTION DECISION SENT FOR APPROVE]: id = [{}], execution id = [{}], to user id = [{}]",
                techConditionEntity.getId(), entity.getId(), userId);
        baseSave(techConditionEntity);

        notificationService.send(userDto.getEmail(), String.format(
                NEW_APPROVAL, IS_NAME, TC_SERVICE_NAME,
                stringOrEmpty(techConditionEntity.getStatementRegistrationNumber()),
                formattedDate(techConditionEntity.getApplicationDatetime())));
    }

    @Override
    public void reSendDecisionForApproval(UUID id, UUID userId) {
        TechConditionExecutionEntity entity = findById(id);
        TechConditionEntity techConditionEntity = entity.getTechCondition();

        checkState(techConditionEntity, Event.TC_RE_SEND_FOR_APPROVAL);

        CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
        if (!entity.getAssignedBy().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NOT_ALLOWED.name());
        }

        UserDto userDto = userApiService.getUserById(userId);

        // for re-sending for approval
        SubdivisionDto subdivisionDto = providerApiService.getSubdivision(userDto.getSubdivisionId());
        entity.setAssignedSubdivision(externalSubdivisionMapper.toEntity(subdivisionDto));
        entity.setOwner(externalUserMapper.toEntity(userDto));

        Set<UUID> executionRelatedUsers = new HashSet<>(entity.getRelatedUsers());
        executionRelatedUsers.add(userId);
        entity.setRelatedUsers(new ArrayList<>(executionRelatedUsers));

        changeState(techConditionEntity, Event.TC_RE_SEND_FOR_APPROVAL);

        techConditionEntity.setAssignees(List.of(userId));
        entity.setAssignees(List.of(userId));
        baseSave(entity);

        log.info("TECH CONDITION [EXECUTION DECISION RE-SENT FOR APPROVE]: id = [{}], execution id = [{}], to user id = [{}]",
                techConditionEntity.getId(), entity.getId(), userId);
        baseSave(techConditionEntity);

        notificationService.send(userDto.getEmail(), String.format(
                NEW_APPROVAL, IS_NAME, TC_SERVICE_NAME,
                stringOrEmpty(techConditionEntity.getStatementRegistrationNumber()),
                formattedDate(techConditionEntity.getApplicationDatetime())));
    }

    @Override
    public void sendDecisionForRevision(UUID id, String reason) {
        TechConditionExecutionEntity entity = findById(id);
        TechConditionEntity techConditionEntity = entity.getTechCondition();

        executionCheckState(techConditionEntity, entity, Event.TCE_SEND_FOR_REVISION);

        entity.setRevisionReason(reason);

        executionChangeState(techConditionEntity, entity, Event.TCE_SEND_FOR_REVISION);

        if (isNull(entity.getExecutor())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.name());
        }
        UUID returnToUserId = entity.getExecutor().getId();

        entity.setAssignees(List.of(returnToUserId));
        baseSave(entity);

        log.info("TECH CONDITION [EXECUTION DECISION SENT FOR REVISION]: id = [{}], execution id = [{}], return to user id = [{}]",
                techConditionEntity.getId(), entity.getId(), returnToUserId);
        baseSave(techConditionEntity);
    }

    @Override
    public void assignDecisionForSign(UUID id, AssignDto dto) {//todo заяв надо остаться в ON_EXECUTION
        TechConditionExecutionEntity entity = findById(id);
        TechConditionEntity techConditionEntity = entity.getTechCondition();

        checkState(techConditionEntity, Event.TC_SEND_FOR_SIGN);

        if (isEmpty(dto.getExecutors())) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
        }
        List<UUID> assignees = new ArrayList<>(dto.getExecutors());
        Set<String> notificationRecipients = new HashSet<>();
//        StringJoiner owners = new StringJoiner(", ");
//        for (UUID assignedUserId : assignees) {
//            UserDto userDto = userApiService.getUserById(assignedUserId);
//            notificationRecipients.add(userDto.getEmail());
//            owners.add(userDto.getFullName());
//        }
//        entity.setOwner(owners.toString());//todo

        Set<UUID> executionRelatedUsers = new HashSet<>(entity.getRelatedUsers());
        executionRelatedUsers.addAll(assignees);
        entity.setRelatedUsers(new ArrayList<>(executionRelatedUsers));

        UserDto userDto = userApiService.getUserById(assignees.get(0));

        if (Objects.equals(techConditionEntity.getStatusCode(), Status.ON_EXECUTION.getCode())) {
            techConditionEntity = approveDecision(techConditionEntity.getId());
        }

        techConditionEntity.setDirector(externalUserMapper.toEntity(userDto));


        changeState(techConditionEntity, Event.TC_SEND_FOR_SIGN);

        techConditionEntity.setAssignees(assignees);
        baseSave(techConditionEntity);

        entity.setAssignees(assignees);
        baseSave(entity);

        String assigneesStrings = assignees.stream()
                .map(UUID::toString)
                .collect(Collectors.joining(", "));
        log.info("TECH CONDITION [EXECUTION DECISION SENT FOR SIGN]: id = [{}], execution id = [{}], to users ids = [{}]",
                techConditionEntity.getId(), entity.getId(), assigneesStrings);

//        String notificationText = null;
//        if (techConditionEntity.getDecisionType() == REASONED_REFUSAL) {
//            notificationText = String.format(NEW_SIGN, IS_NAME, DOC_REASONED_REFUSAL,
//                    formattedDate(techConditionEntity.getApplicationDatetime()));
//        } else if (techConditionEntity.getDecisionType() == TECHNICAL_RECOMMENDATION) {
//            notificationText = String.format(NEW_SIGN, IS_NAME, DOC_TC_PROJECT,
//                    formattedDate(techConditionEntity.getApplicationDatetime()));
//        }
//
//        notificationService.send(mergeEmails(notificationRecipients), notificationText);
    }

    @Override
    public TechConditionEntity approveDecision(UUID id) {//todo если 1 проект ТУ и статус поменялся
        TechConditionExecutionEntity entity = findById(id);
        TechConditionEntity techConditionEntity = entity.getTechCondition();

        checkState(techConditionEntity, Event.TC_APPROVE);

        CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
        techConditionEntity.setManager(externalUserMapper.fromCurrentUserResponse(currentUser));
        techConditionEntity.setManagerSignedDatetime(OffsetDateTime.now());

        changeState(techConditionEntity, Event.TC_APPROVE);

        if (entity.isQuickRefusal()) {
            entity.setStatusCode(ExecutionStatus.APPROVED.getCode());
            baseSave(entity);
        }

        log.info("TECH CONDITION [EXECUTION DECISION APPROVED]: id = [{}], execution id = [{}]",
                techConditionEntity.getId(), entity.getId());

        return baseSave(techConditionEntity);
    }

    @Override
    public void approveDecisionAndSendForSign(UUID id, AssignDto dto) {
        this.approveDecision(id);
        this.assignDecisionForSign(id, dto);
    }*/


    private void updateData(TechConditionEntity entity, Event event) {
        var dto = (TechConditionStatementDto) registryApiService.getByStatementId(entity.getStatementId());
        dto = techConditionMapper.toStatementDto(dto, entity);
        dto.setEvent(event);
        log.info("TECH CONDITION EXECUTION [UPDATE REGISTRY DATA]: event = [{}], dto = [{}],", event, dto);
        registryApiService.updateData(dto.getId(), dto);
    }

    private void checkState(TechConditionEntity entity, Event event) {
        log.info("TECH CONDITION [CHECK-STATE]: status = [{}], event = [{}]", entity.getStatusCode(), event);
        try {
            techConditionStatemachine.checkState(entity, null, entity.getStatusCode(), event);
        } catch (UnknownStateException | UnknownEventException | GuardException e) {
            log.error("TECH CONDITION [CHECK-STATE]: error message = {}", e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }

    private void changeState(TechConditionEntity entity, Event event) {
        log.info("TECH CONDITION [CHANGE-STATE]: status = [{}], event = [{}]", entity.getStatusCode(), event);
        try {
            techConditionStatemachine.changeState(entity, null, entity.getStatusCode(), event);
        } catch (UnknownStateException | UnknownEventException | GuardException e) {
            log.error("TECH CONDITION [CHANGE-STATE]: error message = {}", e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }

    private void executionCheckState(TechConditionEntity entity, TechConditionExecutionEntity execution, Event event) {
        log.info("TECH CONDITION EXECUTION [CHECK-STATE]: status = [{}], event = [{}]", execution.getStatusCode(), event);
        try {
            techConditionExecutionStatemachine.checkState(entity, execution, execution.getStatusCode(), event);
        } catch (UnknownStateException | UnknownEventException | GuardException e) {
            log.error("TECH CONDITION EXECUTION [CHECK-STATE]: error message = {}", e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }

    private void executionChangeState(TechConditionEntity entity, TechConditionExecutionEntity execution, Event event) {
        log.info("TECH CONDITION EXECUTION [CHANGE-STATE]: status = [{}], event = [{}]", execution.getStatusCode(), event);
        try {
            techConditionExecutionStatemachine.changeState(entity, execution, execution.getStatusCode(), event);
        } catch (UnknownStateException | UnknownEventException | GuardException e) {
            log.error("TECH CONDITION EXECUTION [CHANGE-STATE]: error message = {}", e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }
}
