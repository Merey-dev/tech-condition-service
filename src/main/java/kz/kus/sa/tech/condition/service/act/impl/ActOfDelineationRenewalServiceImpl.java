package kz.kus.sa.tech.condition.service.act.impl;

import kz.kus.sa.auth.api.calendar.CalendarEventApiService;
import kz.kus.sa.auth.api.currentuser.CurrentUserApiService;
import kz.kus.sa.auth.api.provider.ProviderApiService;
import kz.kus.sa.auth.api.provider.dto.SubdivisionDto;
import kz.kus.sa.auth.api.user.UserApiService;
import kz.kus.sa.auth.api.user.dto.UserDto;
import kz.kus.sa.auth.api.user.dto.UserFilterDto;
import kz.kus.sa.registry.api.RegistryApiService;
import kz.kus.sa.registry.api.RegistrySignApiService;
import kz.kus.sa.registry.dto.common.AssignDto;
import kz.kus.sa.registry.dto.renewal.ActOfDelineationRenewalDto;
import kz.kus.sa.registry.enums.Event;
import kz.kus.sa.report.api.ReportActOfDelineationRenewalApiService;
import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationRenewalEntity;
import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationRenewalExecutionEntity;
import kz.kus.sa.tech.condition.dao.mapper.ActOfDelineationRenewalMapper;
import kz.kus.sa.tech.condition.dao.mapper.ExternalSubdivisionMapper;
import kz.kus.sa.tech.condition.dao.mapper.ExternalUserMapper;
import kz.kus.sa.tech.condition.dao.repository.ActOfDelineationRenewalRepository;
import kz.kus.sa.tech.condition.exception.BadRequestException;
import kz.kus.sa.tech.condition.exception.ErrorCode;
import kz.kus.sa.tech.condition.exception.NotFoundException;
import kz.kus.sa.tech.condition.service.act.ActOfDelineationRenewalExecutionService;
import kz.kus.sa.tech.condition.service.act.ActOfDelineationRenewalService;
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
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ActOfDelineationRenewalServiceImpl implements ActOfDelineationRenewalService {

    private final UserApiService userApiService;
    private final ProviderApiService providerApiService;
    private final RegistryApiService registryApiService;
    private final ExternalUserMapper externalUserMapper;
    private final CurrentUserApiService currentUserApiService;
    private final RegistrySignApiService registrySignApiService;
    private final CalendarEventApiService calendarEventApiService;
    private final ExternalSubdivisionMapper externalSubdivisionMapper;
    private final ActOfDelineationRenewalMapper actOfDelineationRenewalMapper;
    private final ActOfDelineationReportService actOfDelineationReportService;
    private final ActOfDelineationRenewalRepository actOfDelineationRenewalRepository;
    private final ActOfDelineationRenewalStatemachine actOfDelineationRenewalStatemachine;
    private final ActOfDelineationRenewalExecutionService actOfDelineationRenewalExecutionService;
    private final ReportActOfDelineationRenewalApiService reportActOfDelineationRenewalApiService;

    @Override
    public ActOfDelineationRenewalDto getByStatementId(UUID statementId) {
        var entity = findByStatementId(statementId);
        return actOfDelineationRenewalMapper.toStatementDto(entity);
    }

    @Override
    public void consume(ActOfDelineationRenewalDto dto) {
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

    private void create(ActOfDelineationRenewalDto dto) {
        var entity = actOfDelineationRenewalMapper.toEntity(dto);

        entity = baseSave(entity);

        changeState(entity, null, dto.getEvent());

        log.info("ACT OF DELINEATION RENEWAL [CREATED]: id = [{}]", entity.getId());
    }

    private void update(ActOfDelineationRenewalDto dto) {
        var entity = findByStatementId(dto.getId());
        entity = actOfDelineationRenewalMapper.toEntity(entity, dto);

        checkState(entity, null, dto.getEvent());
        changeState(entity, null, dto.getEvent());

        log.info("ACT OF DELINEATION RENEWAL [UPDATED]: id = [{}]", entity.getId());
        baseSave(entity);

        updateData(entity, dto.getEvent());
    }

    private void delete(ActOfDelineationRenewalDto dto) {
        var entity = findByStatementId(dto.getId());
        entity = actOfDelineationRenewalMapper.toEntity(entity, dto);

        checkState(entity, null, dto.getEvent());
        changeState(entity, null, dto.getEvent());

        log.info("ACT OF DELINEATION RENEWAL [DELETED]: id = [{}]", entity.getId());
        baseSave(entity);
    }

    private void addConsumerSign(ActOfDelineationRenewalDto dto) {
        var entity = findByStatementId(dto.getId());
        entity = actOfDelineationRenewalMapper.toEntity(entity, dto);

        checkState(entity, null, dto.getEvent());
        changeState(entity, null, dto.getEvent());

        log.info("ACT OF DELINEATION RENEWAL [ADDED CONSUMER SIGN]: id = [{}]", entity.getId());
        baseSave(entity);
    }

    private void deleteConsumerSign(ActOfDelineationRenewalDto dto) {
        var entity = findByStatementId(dto.getId());
        entity = actOfDelineationRenewalMapper.toEntity(entity, dto);

        checkState(entity, null, dto.getEvent());
        changeState(entity, null, dto.getEvent());

        log.info("ACT OF DELINEATION RENEWAL [DELETED CONSUMER SIGN]: id = [{}]", entity.getId());
        baseSave(entity);
    }

    private void register(ActOfDelineationRenewalDto dto) {
        var entity = findByStatementId(dto.getId());
        entity = actOfDelineationRenewalMapper.toEntity(entity, dto);

        checkState(entity, null, dto.getEvent());

        entity.setOffHours(isFalse(calendarEventApiService.isProviderWorking(entity.getProviderId())));

        changeState(entity, null, dto.getEvent());

        entity = baseSave(entity);

        log.info("ACT OF DELINEATION RENEWAL [REGISTERED]: id = [{}]", entity.getId());
        updateData(entity, dto.getEvent());
    }

    @Override
    public void takeToExecution(UUID statementId) {
        var entity = findByStatementId(statementId);

        checkState(entity, null, Event.TAKE_TO_EXECUTION);

        var currentUser = currentUserApiService.getCurrentUser();
        setUsersOnEntity(entity, Collections.singletonList(currentUser.getId()), true);

        changeState(entity, null, Event.TAKE_TO_EXECUTION);

        log.info("ACT OF DELINEATION RENEWAL [TAKE TO EXECUTION]: id = [{}], executor = [{}]", entity.getId(), currentUser.getId());
        baseSave(entity);

        updateData(entity, Event.TAKE_TO_EXECUTION);

        //todo send DP
    }

    @Override
    public void assign(UUID statementId, AssignDto dto) {
        var entity = findByStatementId(statementId);

        checkState(entity, null, dto.getEvent());

        var currentUser = currentUserApiService.getCurrentUser();

        List<UUID> assignees = new ArrayList<>();
//        Set<String> notificationRecipients = new HashSet<>();

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
            }
        }
        if (Event.ASSIGN_TO_EXECUTOR == dto.getEvent()) {
            if (isEmpty(dto.getExecutors())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            } else {
                assignees.addAll(dto.getExecutors());

//                dto.getExecutors().forEach(userId ->
//                        notificationRecipients.add(
//                                userApiService.getUserById(userId).getEmail())
//                );

                UserDto userDto = userApiService.getUserById(dto.getExecutors().get(0));
                SubdivisionDto subdivisionDto = providerApiService.getSubdivision(userDto.getSubdivisionId());
                entity.setSubdivision(externalSubdivisionMapper.toEntity(subdivisionDto));
            }
        }
        if (Event.ASSIGN_TO_DIVISION_WITH_ADDRESS == dto.getEvent()) {
            if (isEmpty(dto.getAddressDivisions())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            } else {
                ActOfDelineationRenewalEntity finalEntity = entity;
                dto.getAddressDivisions().forEach(d -> {
                    List<UserDto> users = userApiService.searchAsList(UserFilterDto.builder()
                            .subDivisionIds(List.of(d.getDivision()))
                            .permissions(List.of(Event.TCA_TAKE_TO_EXECUTION.name()))
                            .build());
                    assignees.addAll(
                            users.stream()
                                    .map(UserDto::getId)
                                    .collect(Collectors.toList())
                    );
//                    notificationRecipients.addAll(
//                            users.stream()
//                                    .map(UserDto::getEmail)
//                                    .collect(Collectors.toSet())
//                    );

//                    d.getObjectAbdAddresses().forEach(a -> {
                    ActOfDelineationRenewalExecutionEntity execution = actOfDelineationRenewalExecutionService.create(finalEntity.getId(), dto);
                    execution.setAssignedBy(currentUser.getId());
//                    });
                });
            }
        }
        if (Event.ASSIGN_TO_EXECUTOR_WITH_ADDRESS == dto.getEvent()) {
            if (isEmpty(dto.getAddressExecutors())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            } else {
                ActOfDelineationRenewalEntity finalEntity = entity;
                dto.getAddressExecutors().forEach(e -> {
                    assignees.add(e.getExecutor());

//                    dto.getExecutors().forEach(userId ->
//                            notificationRecipients.add(
//                                    userApiService.getUserById(userId).getEmail())
//                    );

//                    e.getObjectAbdAddresses().forEach(a -> {
                    ActOfDelineationRenewalExecutionEntity execution = actOfDelineationRenewalExecutionService.create(finalEntity.getId(), dto);
                    execution.setAssignedBy(currentUser.getId());
//                    });
                });
            }
        }

        if (assignees.isEmpty()) {
            log.error("ERROR ACT OF DELINEATION RENEWAL [ASSIGNING]: id = [{}], error = [{}]", entity.getId(), ErrorCode.NO_USERS_TO_ASSIGN.name());
            throw new BadRequestException(ErrorCode.NO_USERS_TO_ASSIGN.name());
        }

        ActOfDelineationRenewalExecutionEntity execution = null;
//        if (List.of(Event.ASSIGN_TO_DIVISION, Event.ASSIGN_TO_EXECUTOR).contains(dto.getEvent())) {
//            execution = techConditionExecutionService.create(entity.getId(), dto, TechConditionExecutionType.APPLICATION, false);
//            execution.setAssignedBy(currentUser.getId());
//        }

        changeState(entity, execution, dto.getEvent());

        setUsersOnEntity(entity, Collections.singletonList(currentUser.getId()), false);

        log.info("ACT OF DELINEATION RENEWAL [ASSIGNED]: id = [{}], registration number = [{}], type = [{}], executors = [{}], divisions = [{}]",
                entity.getId(), entity.getStatementRegistrationNumber(), dto.getEvent(), dto.getExecutors(), dto.getDivisions());
        entity = baseSave(entity);

        updateData(entity, dto.getEvent());
    }

    @Override
    public void sendForApproval(UUID statementId, AssignDto dto) {
        var entity = findByStatementId(statementId);

        checkState(entity, null, Event.SEND_FOR_APPROVAL);
        changeState(entity, null, Event.SEND_FOR_APPROVAL);

        setUsersOnEntity(entity, dto.getExecutors(), false);

        log.info("ACT OF DELINEATION RENEWAL [SEND FOR APPROVAL]: id = [{}], executor = [{}]", entity.getId(), dto.getExecutors());
        baseSave(entity);

        updateData(entity, Event.SEND_FOR_APPROVAL);
    }

    @Override
    public void sendForConfirmation(UUID statementId, AssignDto dto) {
        var entity = findByStatementId(statementId);

        checkState(entity, null, Event.SENT_FOR_CONFIRMATION);
        changeState(entity, null, Event.SENT_FOR_CONFIRMATION);

        setUsersOnEntity(entity, dto.getExecutors(), false);

        log.info("ACT OF DELINEATION RENEWAL [SEND FOR CONFIRMATION]: id = [{}], executor = [{}]", entity.getId(), dto.getExecutors());
        baseSave(entity);

        updateData(entity, Event.SENT_FOR_CONFIRMATION);
    }

    @Override
    public void sendForRevision(UUID statementId, AssignDto dto, String reason) {
        var entity = findByStatementId(statementId);

        checkState(entity, null, Event.SEND_FOR_REVISION);
        changeState(entity, null, Event.SEND_FOR_REVISION);

        setUsersOnEntity(entity, dto.getExecutors(), false);
        var currentUser = currentUserApiService.getCurrentUser();
        entity.setRevisionReasonUserId(currentUser.getId());
        entity.setRevisionReason(reason);
        entity.setRevisionReasonDatetime(OffsetDateTime.now());

        log.info("ACT OF DELINEATION RENEWAL [SEND FOR REVISION]: id = [{}], executor = [{}]", entity.getId(), dto.getExecutors());
        baseSave(entity);

        updateData(entity, Event.SEND_FOR_REVISION);
    }

    /*public void providerSign(UUID statementId, SignCreateDto sign) {
        var entity = findByStatementId(statementId);

        checkState(entity, null, Event.PROVIDER_SIGN);

        var currentUser = currentUserApiService.getCurrentUser();
        entity.setDirector(externalUserMapper.fromCurrentUserResponse(currentUser));
        entity.setDirectorApprovedDatetime(OffsetDateTime.now());

        var actReportData = actOfDelineationReportService.actReportData(entity.getId());
        sign.setBase64Data(reportActOfDelineationRenewalApiService.actOfDelineationReportBase64(entity.getId(), actReportData));
        registrySignApiService.addProviderSign(statementId, sign);

        changeState(entity, null, Event.PROVIDER_SIGN);

        log.info("ACT OF DELINEATION RENEWAL [PROVIDER SIGN]: id = [{}]", entity.getId());
        baseSave(entity);

        updateData(entity, Event.PROVIDER_SIGN);
    }

    public void consumerSign(UUID statementId, SignCreateDto sign) {
        var entity = findByStatementId(statementId);

        checkState(entity, null, Event.CONSUMER_SIGN);

        registrySignApiService.addConsumerSign(statementId, sign);

        changeState(entity, null, Event.CONSUMER_SIGN);

        log.info("ACT OF DELINEATION RENEWAL [CONSUMER SIGN]: id = [{}]", entity.getId());
        baseSave(entity);

        updateData(entity, Event.CONSUMER_SIGN);
    }*/

    private ActOfDelineationRenewalEntity baseSave(ActOfDelineationRenewalEntity entity) {
        OffsetDateTime now = OffsetDateTime.now();
        if (isNull(entity.getCreatedDatetime())) {
            entity.setCreatedDatetime(now);
        }
        entity.setLastModifiedDatetime(now);
        return actOfDelineationRenewalRepository.save(entity);
    }

    @Override
    public ActOfDelineationRenewalEntity findById(UUID id) {
        return actOfDelineationRenewalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    private ActOfDelineationRenewalEntity findByStatementId(UUID statementId) {
        return actOfDelineationRenewalRepository.findByStatementIdAndDeletedDatetimeIsNull(statementId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    private void setUsersOnEntity(ActOfDelineationRenewalEntity entity, List<UUID> userIds, boolean isExecutor) {
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
