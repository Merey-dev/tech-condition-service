package condition.service.tech.condition.impl;

import kz.kus.commons.enums.ConsumerType;
import kz.kus.sa.auth.api.calendar.CalendarEventApiService;
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
import kz.kus.sa.registry.dto.common.AssignDto;
import kz.kus.sa.registry.dto.common.FileCreateDto;
import kz.kus.sa.registry.dto.tc.v1.TechConditionStatementDto;
import kz.kus.sa.registry.dto.v1.StatementDto;
import kz.kus.sa.registry.enums.AuthProviderSubDivisionRole;
import kz.kus.sa.registry.enums.Event;
import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionEntity;
import kz.kus.sa.tech.condition.dao.mapper.*;
import kz.kus.sa.tech.condition.dao.repository.TechConditionRepository;
import kz.kus.sa.tech.condition.dto.TechConditionDto;
import kz.kus.sa.tech.condition.enums.ExecutionStatus;
import kz.kus.sa.tech.condition.enums.TechConditionExecutionType;
import kz.kus.sa.tech.condition.exception.BadRequestException;
import kz.kus.sa.tech.condition.exception.BusinessException;
import kz.kus.sa.tech.condition.exception.ErrorCode;
import kz.kus.sa.tech.condition.exception.NotFoundException;
import kz.kus.sa.tech.condition.service.address.AbdAddressService;
import kz.kus.sa.tech.condition.service.address.IntersectionService;
import kz.kus.sa.tech.condition.service.intagration.KzharyqTechConditionService;
import kz.kus.sa.tech.condition.service.notification.NotificationService;
import kz.kus.sa.tech.condition.service.tech.condition.*;
import kz.kus.sa.tech.condition.statemachine.TechConditionStatemachine;
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
import static java.util.Objects.nonNull;
import static kz.kus.sa.tech.condition.util.CommonUtils.*;
import static kz.kus.sa.tech.condition.util.CommonUtils.stringOrEmpty;
import static kz.kus.sa.tech.condition.util.Constants.PROCESSING_DAYS;
import static kz.kus.sa.tech.condition.util.Messages.*;
import static kz.kus.sa.tech.condition.util.Messages.APPLICATION_REGISTERED;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TechConditionServiceImpl implements TechConditionService {

    private final UserApiService userApiService;
    private final AbdAddressMapper abdAddressMapper;
    private final AbdAddressService abdAddressService;
    private final ConsumerApiService consumerApiService;
    private final ProviderApiService providerApiService;
    private final RegistryApiService registryApiService;
    private final ExternalUserMapper externalUserMapper;
    private final ExternalFileMapper externalFileMapper;
    private final IntersectionMapper intersectionMapper;
    private final IntersectionService intersectionService;
    private final TechConditionMapper techConditionMapper;
    private final NotificationService notificationService;
    private final CurrentUserApiService currentUserApiService;
    private final TechConditionRepository techConditionRepository;
    private final CalendarEventApiService calendarEventApiService;
    private final TechConditionSubConsumerMapper subConsumerMapper;
    private final TechConditionMaximumLoadMapper maximumLoadMapper;
    private final TechConditionStatemachine techConditionStatemachine;
    private final ExternalSubdivisionMapper externalSubdivisionMapper;
    private final KzharyqTechConditionService kzharyqTechConditionService;
    private final TechConditionPlannedEquipmentMapper plannedEquipmentMapper;
    private final TechConditionExecutionService techConditionExecutionService;
    private final TechConditionSubConsumerService techConditionSubConsumerService;
    private final TechConditionMaximumLoadService techConditionMaximumLoadService;
    private final TechConditionReliabilityCategoryMapper reliabilityCategoryMapper;
    private final TechConditionPlannedEquipmentService techConditionPlannedEquipmentService;
    private final TechConditionReliabilityCategoryService techConditionReliabilityCategoryService;
    private final TechConditionContractualCapacityOfTransformerMapper contractualCapacityOfTransformerMapper;
    private final TechConditionContractualCapacityOfTransformerService techConditionContractualCapacityOfTransformerService;

    @Override
    public TechConditionStatementDto getByStatementId(UUID statementId) {
        var entity = findByStatementId(statementId);
        return techConditionMapper.toStatementDto(entity);
    }

    @Override
    public void consume(TechConditionStatementDto dto) {
        switch (dto.getEvent()) {
            case CREATE:
                create(dto);
                break;
            case UPDATE:
                update(dto);
                break;
            case DELETE:
                delete(dto);
                break;
            case ADD_CONSUMER_SIGN:
                addConsumerSign(dto);
                break;
            case DELETE_CONSUMER_SIGN:
                deleteConsumerSign(dto);
                break;
            case REGISTER:
                register(dto);
                break;
            default:
                log.error("Unknown event type [{}]", dto.getEvent());
                break;
        }
    }

    private void create(TechConditionStatementDto dto) {
        TechConditionEntity entity = techConditionMapper.toEntity(dto);

        entity = baseSave(entity);

        abdAddressService.saveList(null, abdAddressMapper.toEntityList(dto.getObjectAbdAddresses()), entity);
        intersectionService.saveList(null, intersectionMapper.toEntityList(dto.getIntersections()), entity);
        techConditionSubConsumerService.saveList(null, subConsumerMapper.toEntityList(dto.getSubConsumers()), entity);
        techConditionMaximumLoadService.saveList(null, maximumLoadMapper.toEntityList(dto.getMaximumLoads()), entity);
        techConditionPlannedEquipmentService.saveList(null, plannedEquipmentMapper.toEntityList(dto.getPlannedEquipments()), entity);
        techConditionContractualCapacityOfTransformerService.saveList(null, contractualCapacityOfTransformerMapper.toEntityList(dto.getContractualCapacityOfTransformers()), entity);
        techConditionReliabilityCategoryService.saveList(null, reliabilityCategoryMapper.toEntityList(dto.getReliabilityCategories()), entity);

        changeState(entity, null, dto.getEvent());

        log.info("TECH CONDITION [CREATED]: id = [{}]", entity.getId());
    }

    private void update(TechConditionStatementDto dto) {
        TechConditionEntity dbEntity = findByStatementId(dto.getId());
        TechConditionEntity entity = techConditionMapper.toEntity(dbEntity, dto);

        checkState(entity, null, dto.getEvent());

        abdAddressService.saveList(dbEntity.getObjectAbdAddresses(), abdAddressMapper.toEntityList(dto.getObjectAbdAddresses()), entity);
        intersectionService.saveList(dbEntity.getIntersections(), intersectionMapper.toEntityList(dto.getIntersections()), entity);
        techConditionSubConsumerService.saveList(dbEntity.getSubConsumers(), subConsumerMapper.toEntityList(dto.getSubConsumers()), entity);
        techConditionMaximumLoadService.saveList(dbEntity.getMaximumLoads(), maximumLoadMapper.toEntityList(dto.getMaximumLoads()), entity);
        techConditionPlannedEquipmentService.saveList(dbEntity.getPlannedEquipments(), plannedEquipmentMapper.toEntityList(dto.getPlannedEquipments()), entity);
        techConditionContractualCapacityOfTransformerService.saveList(dbEntity.getContractualCapacityOfTransformers(), contractualCapacityOfTransformerMapper.toEntityList(dto.getContractualCapacityOfTransformers()), entity);
        techConditionReliabilityCategoryService.saveList(dbEntity.getReliabilityCategories(), reliabilityCategoryMapper.toEntityList(dto.getReliabilityCategories()), entity);

        changeState(entity, null, dto.getEvent());

        log.info("TECH CONDITION [UPDATED]: id = [{}]", entity.getId());
        baseSave(entity);
    }

    private void delete(TechConditionStatementDto dto) {
        TechConditionEntity dbEntity = findByStatementId(dto.getId());
        TechConditionEntity entity = techConditionMapper.toEntity(dbEntity, dto);

        checkState(entity, null, dto.getEvent());

        abdAddressService.deleteDatetime(dbEntity.getObjectAbdAddresses());

        changeState(entity, null, dto.getEvent());

        log.info("TECH CONDITION [DELETED]: id = [{}]", entity.getId());
        baseSave(entity);
    }

    private void addConsumerSign(TechConditionStatementDto dto) {
        TechConditionEntity dbEntity = findByStatementId(dto.getId());
        TechConditionEntity entity = techConditionMapper.toEntity(dbEntity, dto);

        checkState(entity, null, dto.getEvent());
        changeState(entity, null, dto.getEvent());

        log.info("TECH CONDITION [ADDED CONSUMER SIGN]: id = [{}]", entity.getId());
        baseSave(entity);
    }

    private void deleteConsumerSign(TechConditionStatementDto dto) {
        TechConditionEntity dbEntity = findByStatementId(dto.getId());
        TechConditionEntity entity = techConditionMapper.toEntity(dbEntity, dto);

        checkState(entity, null, dto.getEvent());
        changeState(entity, null, dto.getEvent());

        log.info("TECH CONDITION [DELETED CONSUMER SIGN]: id = [{}]", entity.getId());
        baseSave(entity);
    }

    private void register(TechConditionStatementDto dto) {
        TechConditionEntity dbEntity = findByStatementId(dto.getId());
        TechConditionEntity entity = techConditionMapper.toEntity(dbEntity, dto);

        checkState(entity, null, dto.getEvent());

        entity.setApplicationDatetime(OffsetDateTime.now());
        entity.setDeadlineDatetime(calendarEventApiService.calculateDeadline(entity.getProviderId(), PROCESSING_DAYS)
                .atTime(23, 59, 59));
        entity.setOffHours(isFalse(calendarEventApiService.isProviderWorking(entity.getProviderId())));

        changeState(entity, null, dto.getEvent());

        entity = baseSave(entity);

        log.info("TECH CONDITION [REGISTERED]: id = [{}]", entity.getId());
        updateData(entity, dto.getEvent());

        kzharyqTechConditionService.sendRegisteredRequest(entity);

//        todo  send DP

        autoAssign(entity, dto.getCurrentUserId());
    }

    private void autoAssign(TechConditionEntity entity, UUID currentUserId) {
        ProviderDto providerDto = providerApiService.getProviderDto(entity.getProviderId());
        List<SubdivisionDto> subDivisions = providerApiService.getSubdivisionsByBinAndRole(
                providerDto.getIinBin(),
                AuthProviderSubDivisionRole.TC_EXECUTOR.name());

        if (subDivisions.size() != 1) {
            String errorCode = subDivisions.isEmpty()
                    ? ErrorCode.EXECUTOR_SUBDIVISION_NOT_FOUND.name()
                    : ErrorCode.TOO_MANY_EXECUTOR_SUBDIVISIONS.name();
            log.error("TECH CONDITION ERROR [AUTO ASSIGNING]: id = [{}], error = [{}]", entity.getId(), errorCode);
            throw new BusinessException(errorCode);
        }

        SubdivisionDto subDivision = subDivisions.get(0);
        log.info("TECH CONDITION [AUTO ASSIGNED]: id = [{}], registration number = [{}], executor division id = [{}]",
                entity.getId(), entity.getStatementRegistrationNumber(), subDivision.getId());

        autoAssignDivision(entity,
                AssignDto.builder()
                        .event(Event.ASSIGN_TO_DIVISION)
                        .divisions(List.of(subDivision.getId()))
                        .build(),
                userApiService.getUserById(currentUserId));
    }

    private void autoAssignDivision(TechConditionEntity entity, AssignDto dto, UserDto currentUser) {
        checkState(entity, null, dto.getEvent());

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

                entity.setSubdivision(externalSubdivisionMapper.toEntity(subdivisions.get(0)));

                List<UserDto> users = userApiService.searchAsList(UserFilterDto.builder()
                        .subDivisionIds(dto.getDivisions())
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
            }
        }
        if (assignees.isEmpty()) {
            log.error("ERROR TECH CONDITION [ASSIGNING]: id = [{}], error = [{}]", entity.getId(), ErrorCode.NO_USERS_TO_ASSIGN.name());
            throw new BadRequestException(ErrorCode.NO_USERS_TO_ASSIGN.name());
        }

        //todo first assigning execution
        TechConditionExecutionEntity execution = null;
        if (List.of(Event.ASSIGN_TO_DIVISION, Event.ASSIGN_TO_EXECUTOR).contains(dto.getEvent())) {
            execution = techConditionExecutionService.create(entity.getId(), dto, TechConditionExecutionType.APPLICATION, false, abdAddressMapper.toDtoList(entity.getObjectAbdAddresses()));
            execution.setAssignedBy(currentUser.getId());
        }

        changeState(entity, execution, dto.getEvent());

        Set<UUID> relatedUsers = new HashSet<>(entity.getRelatedUsers());
        relatedUsers.addAll(assignees);
        entity.setAssignedBy(currentUser.getId());
        entity.setCurrentUserId(currentUser.getId());
        entity.setAssignees(new ArrayList<>()); // todo remove the executors because executions will run after that
        entity.setRelatedUsers(new ArrayList<>(relatedUsers));
        entity.setHasActiveExecutions(true);

        log.info("TECH CONDITION [ASSIGNED]: id = [{}], registration number = [{}], type = [{}], executors = [{}], divisions = [{}]",
                entity.getId(), entity.getStatementRegistrationNumber(), dto.getEvent(), dto.getExecutors(), dto.getDivisions());
        entity = baseSave(entity);

        updateData(entity, dto.getEvent());

//        notificationSend(entity, notificationRecipients, currentUser);
    }

    @Override
    public TechConditionDto getTechConditionByStatementId(UUID statementId) {
        return techConditionMapper.toDto(findByStatementId(statementId));
    }

    @Override
    public void assign(UUID statementId, AssignDto dto) {
        TechConditionEntity entity = findByStatementId(statementId);

        checkState(entity, null, dto.getEvent());

        CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();

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

                entity.setSubdivision(externalSubdivisionMapper.toEntity(subdivisions.get(0)));

                List<UserDto> users = userApiService.searchAsList(UserFilterDto.builder()
                        .subDivisionIds(dto.getDivisions())
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

                UserDto userDto = userApiService.getUserById(dto.getExecutors().get(0));
                SubdivisionDto subdivisionDto = providerApiService.getSubdivision(userDto.getSubdivisionId());
                entity.setSubdivision(externalSubdivisionMapper.toEntity(subdivisionDto));
            }
        }
        if (Event.ASSIGN_TO_DIVISION_WITH_ADDRESS == dto.getEvent()) {
            if (isEmpty(dto.getAddressDivisions())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            } else {
                TechConditionEntity finalEntity = entity;
                dto.getAddressDivisions().forEach(d -> {
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
//                    List<AbdAddressEntity> filterAddress = finalEntity.getAddresses();
//                    filterAddress.retainAll(abdAddressMapper.toEntityList(d.getAddresses()));
//
//                    abdAddressService.saveAssignees(filterAddress, assignees, finalEntity);
//                    d.getObjectAbdAddresses().forEach(a -> {
                    TechConditionExecutionEntity execution = techConditionExecutionService.create(finalEntity.getId(),
                            dto, TechConditionExecutionType.APPLICATION, false, d.getObjectAbdAddresses());
                    execution.setAssignedBy(currentUser.getId());
//                    });
                });
                entity.setGovAgency(true);
            }
        }
        if (Event.ASSIGN_TO_EXECUTOR_WITH_ADDRESS == dto.getEvent()) {
            if (isEmpty(dto.getAddressExecutors())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            } else {
                TechConditionEntity finalEntity = entity;
                dto.getAddressExecutors().forEach(e -> {
                    assignees.add(e.getExecutor());
                    notificationRecipients.add(userApiService.getUserById(e.getExecutor()).getEmail());
//                    List<AbdAddressEntity> filterAddress = finalEntity.getAddresses();
//                    filterAddress.retainAll(abdAddressMapper.toEntityList(e.getAddresses()));
//
//                    abdAddressService.saveAssignees(filterAddress, assignees, finalEntity);
//                    e.getObjectAbdAddresses().forEach(a -> {
                    TechConditionExecutionEntity execution = techConditionExecutionService.create(finalEntity.getId(),
                            dto, TechConditionExecutionType.APPLICATION, false, e.getObjectAbdAddresses());
                    execution.setAssignedBy(currentUser.getId());
//                    });
                });
                entity.setGovAgency(true);
            }
        }

        if (assignees.isEmpty()) {
            log.error("ERROR TECH CONDITION [ASSIGNING]: id = [{}], error = [{}]", entity.getId(), ErrorCode.NO_USERS_TO_ASSIGN.name());
            throw new BadRequestException(ErrorCode.NO_USERS_TO_ASSIGN.name());
        }

        //todo first assigning execution
        TechConditionExecutionEntity execution = null;
        if (List.of(Event.ASSIGN_TO_DIVISION, Event.ASSIGN_TO_EXECUTOR).contains(dto.getEvent())) {
            execution = techConditionExecutionService.create(entity.getId(), dto, TechConditionExecutionType.APPLICATION, false, abdAddressMapper.toDtoList(entity.getObjectAbdAddresses()));
            execution.setAssignedBy(currentUser.getId());
        }

        changeState(entity, execution, dto.getEvent());

        Set<UUID> relatedUsers = new HashSet<>(entity.getRelatedUsers());
        relatedUsers.addAll(assignees);
        entity.setAssignedBy(currentUser.getId());
        entity.setCurrentUserId(currentUser.getId());
        entity.setAssignees(new ArrayList<>()); // todo remove the executors because executions will run after that
        entity.setRelatedUsers(new ArrayList<>(relatedUsers));
        entity.setHasActiveExecutions(true);

        log.info("TECH CONDITION [ASSIGNED]: id = [{}], registration number = [{}], type = [{}], executors = [{}], divisions = [{}]",
                entity.getId(), entity.getStatementRegistrationNumber(), dto.getEvent(), dto.getExecutors(), dto.getDivisions());
        entity = baseSave(entity);

        updateData(entity, dto.getEvent());

        notificationSend(entity, notificationRecipients, currentUser);
    }

    @Override
    public void reAssign(UUID statementId, AssignDto dto) {
        TechConditionEntity entity = findByStatementId(statementId);

        List<TechConditionExecutionEntity> executionEntityList = techConditionExecutionService.findAllByTechConditionId(entity.getId());
        if (isFalse(entity.getGovAgency()) && executionEntityList.size() != 1) {
            throw new BusinessException(ErrorCode.NOT_ALLOWED.name());
        }

        checkState(entity, null, dto.getEvent());

        List<UUID> assignees = new ArrayList<>();
        TechConditionExecutionEntity execution = executionEntityList.get(0);

        if (Event.ASSIGN_TO_DIVISION == dto.getEvent()) {
            if (isEmpty(dto.getDivisions())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            } else {
                List<UserDto> users = userApiService.searchAsList(UserFilterDto.builder()
                        .subDivisionIds(dto.getDivisions())
                        .permissions(List.of(Event.TCE_TAKE_TO_EXECUTION.name()))// todo TAKE_TO_EXECUTION
                        .build());
                assignees.addAll(
                        users.stream()
                                .map(UserDto::getId)
                                .collect(Collectors.toList())
                );

                SubdivisionDto subdivision = providerApiService.getSubdivision(dto.getDivisions().get(0));
                execution.setAssignedSubdivision(externalSubdivisionMapper.toEntity(subdivision));
            }
        }
        if (Event.ASSIGN_TO_EXECUTOR == dto.getEvent()) {
            if (isEmpty(dto.getExecutors())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            } else {
                assignees.addAll(dto.getExecutors());

                UserDto userDto = userApiService.getUserById(dto.getExecutors().get(0));
                execution.setAssignedExecutor(externalUserMapper.toEntity(userDto));
            }
        }
        if (Event.ASSIGN_TO_DIVISION_WITH_ADDRESS == dto.getEvent()) {
            if (isEmpty(dto.getAddressDivisions())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            } else {
                TechConditionEntity finalEntity = entity;
                List<SubdivisionDto> subdivisionList = new ArrayList<>();

                dto.getAddressDivisions().forEach(d -> {
                    List<UserDto> users = userApiService.searchAsList(UserFilterDto.builder()
                            .subDivisionIds(List.of(d.getDivision()))
                            .permissions(List.of(Event.TCE_TAKE_TO_EXECUTION.name()))// todo TAKE_TO_EXECUTION
                            .build());
                    assignees.addAll(
                            users.stream()
                                    .map(UserDto::getId)
                                    .collect(Collectors.toList())
                    );

                    subdivisionList.add(providerApiService.getSubdivision(d.getDivision()));

//                    List<AbdAddressEntity> filterAddress = finalEntity.getAddresses();
//                    filterAddress.retainAll(abdAddressMapper.toEntityList(d.getAddresses()));
//
//                    abdAddressService.saveAssignees(filterAddress, assignees, finalEntity);
                });

//                execution.setAssignedSubdivision(externalSubdivisionMapper.toEntity(subdivisionList.get(0)));
            }
        }
        if (Event.ASSIGN_TO_EXECUTOR_WITH_ADDRESS == dto.getEvent()) {
            if (isEmpty(dto.getAddressExecutors())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            } else {
                TechConditionEntity finalEntity = entity;
//                List<UserDto> userList = new ArrayList<>();

                dto.getAddressExecutors().forEach(e -> {
                    assignees.add(e.getExecutor());

//                    userList.add(userApiService.getUserById(e.getExecutor()));

//                    List<AbdAddressEntity> filterAddress = finalEntity.getAddresses();
//                    filterAddress.retainAll(abdAddressMapper.toEntityList(e.getAddresses()));
//
//                    abdAddressService.saveAssignees(filterAddress, assignees, finalEntity);
                });

//                execution.setAssignedExecutor(externalUserMapper.toEntity(userList.get(0)));
            }
        }

        if (assignees.isEmpty()) {
            log.error("ERROR TECH CONDITION [RE-ASSIGNING]: id = [{}], error = [{}]", entity.getId(), ErrorCode.NO_USERS_TO_ASSIGN.name());
            throw new BadRequestException(ErrorCode.NO_USERS_TO_ASSIGN.name());
        }

        execution.setAssignees(assignees);
        Set<UUID> newRelatedUsers = new HashSet<>(assignees);
        newRelatedUsers.add(entity.getInitiatorId());
        execution.setRelatedUsers(new ArrayList<>(newRelatedUsers));
        techConditionExecutionService.baseSave(execution);

        entity.setRelatedUsers(new ArrayList<>(newRelatedUsers));

        changeState(entity, execution, dto.getEvent());

        log.info("TECH CONDITION [RE-ASSIGNED]: id = [{}], registration number = [{}], type = [{}], executors = [{}], divisions = [{}]",
                entity.getId(), entity.getStatementRegistrationNumber(), dto.getEvent(), dto.getExecutors(), dto.getDivisions());
        entity = baseSave(entity);

        updateData(entity, dto.getEvent());
    }

    @Override
    public void returnToConsumer(UUID statementId, String comment) {
        TechConditionEntity entity = findByStatementId(statementId);

        checkState(entity, null, Event.RETURN_TO_CONSUMER);

        entity.setReturnToCustomerDate(OffsetDateTime.now());
        entity.setReturnToCustomerComment(comment);

        CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
        Set<UUID> relatedUsers = new HashSet<>(entity.getRelatedUsers());
        relatedUsers.add(currentUser.getId());

        entity.setAssignees(List.of(currentUser.getId()));
        entity.setRelatedUsers(new ArrayList<>(relatedUsers));

        List<TechConditionExecutionEntity> executions = techConditionExecutionService.findAllByTechConditionId(entity.getId());
        List<String> executionsIds = new ArrayList<>();
        for (TechConditionExecutionEntity execution : executions) {
            if (!execution.getStatusCode().equals(ExecutionStatus.RETURNED_TO_CONSUMER.getCode())) {
                executionsIds.add(execution.getId().toString());
                execution.setStatusCode(ExecutionStatus.RETURNED_TO_CONSUMER.getCode());
                execution.setAssignees(List.of(currentUser.getId()));
                Set<UUID> executionRelatedUsers = new HashSet<>(execution.getRelatedUsers());
                executionRelatedUsers.add(currentUser.getId());
                execution.setRelatedUsers(new ArrayList<>(executionRelatedUsers));
                techConditionExecutionService.baseSave(execution);
            }
        }

        changeState(entity, null, Event.RETURN_TO_CONSUMER);

        //todo send DP

        log.info("TECH CONDITION [RETURNED TO CONSUMER]: id = [{}], executions ids = [{}]", entity.getId(), String.join(", ", executionsIds));
        entity = baseSave(entity);

        updateData(entity, Event.RETURN_TO_CONSUMER);
    }

    @Override
    public void refuseByConsumer(UUID statementId, FileCreateDto dto) {
        TechConditionEntity entity = findByStatementId(statementId);

        checkState(entity, null, Event.REFUSE);

        entity.setConsumerRefusalFile(externalFileMapper.toEntity(dto));

        List<TechConditionExecutionEntity> executions = techConditionExecutionService.findAllByTechConditionId(entity.getId());
        List<String> executionsIds = new ArrayList<>();
        for (TechConditionExecutionEntity execution : executions) {
            if (!execution.getStatusCode().equals(ExecutionStatus.REFUSED_BY_CONSUMER.getCode())) {
                executionsIds.add(execution.getId().toString());
                execution.setStatusCode(ExecutionStatus.REFUSED_BY_CONSUMER.getCode());
                techConditionExecutionService.baseSave(execution);
            }
        }

        changeState(entity, null, Event.REFUSE);

        log.info("TECH CONDITION [REFUSED BY CONSUMER]: id = [{}], executions ids = [{}]", entity.getId(), String.join(", ", executionsIds));
        entity = baseSave(entity);

        updateData(entity, Event.REFUSE);

//        todo send DP

        kzharyqTechConditionService.sendCompletedRequest(entity);
    }

    private TechConditionEntity baseSave(TechConditionEntity entity) {
        OffsetDateTime now = OffsetDateTime.now();
        if (isNull(entity.getCreatedDatetime())) {
            entity.setCreatedDatetime(now);
        }
        entity.setLastModifiedDatetime(now);
        return techConditionRepository.save(entity);
    }

    private TechConditionEntity findByStatementId(UUID statementId) {
        return techConditionRepository.findByStatementIdAndDeletedDatetimeIsNull(statementId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    private void updateData(TechConditionEntity entity, Event event) {
        var dto = (TechConditionStatementDto) registryApiService.getByStatementId(entity.getStatementId());
        dto = techConditionMapper.toStatementDto(dto, entity);
        dto.setEvent(event);
        log.info("TECH CONDITION [UPDATE REGISTRY DATA]: event = [{}], dto = [{}],", event, dto);
        registryApiService.updateData(dto.getId(), dto);
    }

    private void notificationSend(TechConditionEntity entity, Set<String> notificationRecipients, CurrentUserResponse currentUser) {
        notificationService.send(mergeEmails(notificationRecipients), String.format(
                NEW_APPLICATION, IS_NAME, TC_SERVICE_NAME,
                stringOrEmpty(entity.getStatementRegistrationNumber()),
                formattedDate(entity.getApplicationDatetime())));

        ProviderDto providerDto = providerApiService.getProviderDto(entity.getProviderId());
        StatementDto statementDto = registryApiService.getByStatementId(entity.getStatementId());

        var consumer = consumerApiService.getConsumer(
                statementDto.getConsumerIinBin(),
                providerDto.getIinBin(),
                ConsumerType.valueOf(statementDto.getConsumerType().name()));

        if (nonNull(consumer) && nonNull(consumer.getEmail()))
            notificationService.send(consumer.getEmail(), String.format(
                    APPLICATION_REGISTERED,
                    stringOrEmpty(providerDto.getKk()),
                    stringOrEmpty(entity.getStatementRegistrationNumber()),
                    stringOrEmpty(providerDto.getRu()),
                    stringOrEmpty(entity.getStatementRegistrationNumber()),
                    stringOrEmpty(currentUser.getFullName()),
                    stringOrEmpty(currentUser.getPhone())));
        else
            log.error("ECH CONDITION Consumer [{},{},{}] not found or email is null",
                    statementDto.getConsumerIinBin(),
                    currentUser.getOrganizationBin(),
                    statementDto.getConsumerType());
    }

    private void checkState(TechConditionEntity entity, TechConditionExecutionEntity execution, Event event) {
        log.info("TECH CONDITION [CHECK-STATE]: status = [{}], event = [{}]", entity.getStatusCode(), event);
        try {
            techConditionStatemachine.checkState(entity, execution, entity.getStatusCode(), event);
        } catch (UnknownStateException | UnknownEventException | GuardException e) {
            log.error("TECH CONDITION [CHECK-STATE]: error message = {}", e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }

    private void changeState(TechConditionEntity entity, TechConditionExecutionEntity execution, Event event) {
        log.info("TECH CONDITION [CHANGE-STATE]: status = [{}], event = [{}]", entity.getStatusCode(), event);
        try {
            techConditionStatemachine.changeState(entity, execution, entity.getStatusCode(), event);
        } catch (UnknownStateException | UnknownEventException | GuardException e) {
            log.error("TECH CONDITION [CHANGE-STATE]: error message = {}", e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }

    @Override
    public TechConditionStatementDto exampleConsume(TechConditionStatementDto dto) {
        switch (dto.getEvent()) {
            case CREATE:
                return exampleCreate(dto);
            case UPDATE:
                update(dto);
                break;
            case DELETE:
                delete(dto);
                break;
            case ADD_CONSUMER_SIGN:
                return exampleAddConsumerSign(dto);
            case DELETE_CONSUMER_SIGN:
                deleteConsumerSign(dto);
                break;
            case REGISTER:
                return exampleRegister(dto);
            default:
                log.error("Unknown event type [{}]", dto.getEvent());
                break;
        }
        return null;
    }

    private TechConditionStatementDto exampleCreate(TechConditionStatementDto dto) {
        TechConditionEntity entity = techConditionMapper.toEntity(dto);

        entity = baseSave(entity);

        abdAddressService.saveList(null, abdAddressMapper.toEntityList(dto.getObjectAbdAddresses()), entity);
        intersectionService.saveList(null, intersectionMapper.toEntityList(dto.getIntersections()), entity);
        techConditionSubConsumerService.saveList(null, subConsumerMapper.toEntityList(dto.getSubConsumers()), entity);
        techConditionMaximumLoadService.saveList(null, maximumLoadMapper.toEntityList(dto.getMaximumLoads()), entity);
        techConditionPlannedEquipmentService.saveList(null, plannedEquipmentMapper.toEntityList(dto.getPlannedEquipments()), entity);
        techConditionContractualCapacityOfTransformerService.saveList(null, contractualCapacityOfTransformerMapper.toEntityList(dto.getContractualCapacityOfTransformers()), entity);
        techConditionReliabilityCategoryService.saveList(null, reliabilityCategoryMapper.toEntityList(dto.getReliabilityCategories()), entity);

        changeState(entity, null, dto.getEvent());

        log.info("TECH CONDITION [CREATED]: id = [{}]", entity.getId());
        return techConditionMapper.toStatementDto(entity);
    }

    private TechConditionStatementDto exampleAddConsumerSign(TechConditionStatementDto dto) {
        TechConditionEntity dbEntity = findByStatementId(dto.getId());
        TechConditionEntity entity = techConditionMapper.toEntity(dbEntity, dto);

        checkState(entity, null, dto.getEvent());

        abdAddressService.saveList(dbEntity.getObjectAbdAddresses(), abdAddressMapper.toEntityList(dto.getObjectAbdAddresses()), entity);
        intersectionService.saveList(dbEntity.getIntersections(), intersectionMapper.toEntityList(dto.getIntersections()), entity);
        techConditionSubConsumerService.saveList(dbEntity.getSubConsumers(), subConsumerMapper.toEntityList(dto.getSubConsumers()), entity);
        techConditionMaximumLoadService.saveList(dbEntity.getMaximumLoads(), maximumLoadMapper.toEntityList(dto.getMaximumLoads()), entity);
        techConditionPlannedEquipmentService.saveList(dbEntity.getPlannedEquipments(), plannedEquipmentMapper.toEntityList(dto.getPlannedEquipments()), entity);
        techConditionContractualCapacityOfTransformerService.saveList(dbEntity.getContractualCapacityOfTransformers(), contractualCapacityOfTransformerMapper.toEntityList(dto.getContractualCapacityOfTransformers()), entity);
        techConditionReliabilityCategoryService.saveList(dbEntity.getReliabilityCategories(), reliabilityCategoryMapper.toEntityList(dto.getReliabilityCategories()), entity);

        changeState(entity, null, dto.getEvent());

        log.info("TECH CONDITION [ADDED CONSUMER SIGN]: id = [{}]", entity.getId());
        baseSave(entity);

        return techConditionMapper.toStatementDto(entity);
    }

    private TechConditionStatementDto exampleRegister(TechConditionStatementDto dto) {
        TechConditionEntity dbEntity = findByStatementId(dto.getId());
        TechConditionEntity entity = techConditionMapper.toEntity(dbEntity, dto);

        checkState(entity, null, dto.getEvent());

        abdAddressService.saveList(dbEntity.getObjectAbdAddresses(), abdAddressMapper.toEntityList(dto.getObjectAbdAddresses()), entity);
        intersectionService.saveList(dbEntity.getIntersections(), intersectionMapper.toEntityList(dto.getIntersections()), entity);
        techConditionSubConsumerService.saveList(dbEntity.getSubConsumers(), subConsumerMapper.toEntityList(dto.getSubConsumers()), entity);
        techConditionMaximumLoadService.saveList(dbEntity.getMaximumLoads(), maximumLoadMapper.toEntityList(dto.getMaximumLoads()), entity);
        techConditionPlannedEquipmentService.saveList(dbEntity.getPlannedEquipments(), plannedEquipmentMapper.toEntityList(dto.getPlannedEquipments()), entity);
        techConditionContractualCapacityOfTransformerService.saveList(dbEntity.getContractualCapacityOfTransformers(), contractualCapacityOfTransformerMapper.toEntityList(dto.getContractualCapacityOfTransformers()), entity);
        techConditionReliabilityCategoryService.saveList(dbEntity.getReliabilityCategories(), reliabilityCategoryMapper.toEntityList(dto.getReliabilityCategories()), entity);

        entity.setApplicationDatetime(OffsetDateTime.now());
        entity.setDeadlineDatetime(calendarEventApiService.calculateDeadline(entity.getProviderId(), PROCESSING_DAYS)
                .atTime(23, 59, 59));
        entity.setOffHours(isFalse(calendarEventApiService.isProviderWorking(entity.getProviderId())));

        changeState(entity, null, dto.getEvent());

        entity = baseSave(entity);

        log.info("TECH CONDITION [REGISTERED]: id = [{}]", entity.getId());
        updateData(entity, dto.getEvent());

        kzharyqTechConditionService.sendRegisteredRequest(entity);

//        todo  send DP

        autoAssign(entity, dto.getCurrentUserId());

        return techConditionMapper.toStatementDto(entity);
    }
}
