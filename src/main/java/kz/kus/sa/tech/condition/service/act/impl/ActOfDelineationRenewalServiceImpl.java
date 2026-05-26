package kz.kus.sa.tech.condition.service.act.impl;

import kz.kus.sa.auth.api.calendar.CalendarEventApiService;
import kz.kus.sa.auth.api.currentuser.CurrentUserApiService;
import kz.kus.sa.auth.api.provider.ProviderApiService;
import kz.kus.sa.auth.api.provider.dto.SubdivisionDto;
import kz.kus.sa.auth.api.user.UserApiService;
import kz.kus.sa.auth.api.user.dto.UserDto;
import kz.kus.sa.auth.api.user.dto.UserFilterDto;
import kz.kus.sa.registry.api.RegistryApiService;
import kz.kus.sa.registry.dto.common.AssignDto;
import kz.kus.sa.registry.dto.renewal.ActOfDelineationRenewalDto;
import kz.kus.sa.registry.enums.Event;
import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationRenewalEntity;
import kz.kus.sa.tech.condition.dao.mapper.ActOfDelineationRenewalMapper;
import kz.kus.sa.tech.condition.dao.mapper.ExternalSubdivisionMapper;
import kz.kus.sa.tech.condition.dao.repository.ActOfDelineationRenewalRepository;
import kz.kus.sa.tech.condition.exception.BadRequestException;
import kz.kus.sa.tech.condition.exception.ErrorCode;
import kz.kus.sa.tech.condition.exception.NotFoundException;
import kz.kus.sa.tech.condition.service.act.ActOfDelineationRenewalAbdAddressDecisionService;
import kz.kus.sa.tech.condition.service.act.ActOfDelineationRenewalService;
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
    private final CurrentUserApiService currentUserApiService;
    private final CalendarEventApiService calendarEventApiService;
    private final ExternalSubdivisionMapper externalSubdivisionMapper;
    private final ActOfDelineationRenewalMapper actOfDelineationRenewalMapper;
    private final ActOfDelineationRenewalRepository actOfDelineationRenewalRepository;
    private final ActOfDelineationRenewalStatemachine actOfDelineationRenewalStatemachine;
    private final ActOfDelineationRenewalAbdAddressDecisionService decisionService;

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
        changeState(entity, dto.getEvent());
        log.info("AOD RENEWAL [CREATED]: id = [{}]", entity.getId());
    }

    private void update(ActOfDelineationRenewalDto dto) {
        var entity = findByStatementId(dto.getId());
        entity = actOfDelineationRenewalMapper.toEntity(entity, dto);

        checkState(entity, dto.getEvent());
        changeState(entity, dto.getEvent());

        log.info("AOD RENEWAL [UPDATED]: id = [{}]", entity.getId());
        baseSave(entity);

        updateData(entity, dto.getEvent());
    }

    private void delete(ActOfDelineationRenewalDto dto) {
        var entity = findByStatementId(dto.getId());
        entity = actOfDelineationRenewalMapper.toEntity(entity, dto);

        checkState(entity, dto.getEvent());
        changeState(entity, dto.getEvent());

        log.info("AOD RENEWAL [DELETED]: id = [{}]", entity.getId());
        baseSave(entity);
    }

    private void addConsumerSign(ActOfDelineationRenewalDto dto) {
        var entity = findByStatementId(dto.getId());
        entity = actOfDelineationRenewalMapper.toEntity(entity, dto);

        checkState(entity, dto.getEvent());
        changeState(entity, dto.getEvent());

        log.info("AOD RENEWAL [ADDED CONSUMER SIGN]: id = [{}]", entity.getId());
        baseSave(entity);
    }

    private void deleteConsumerSign(ActOfDelineationRenewalDto dto) {
        var entity = findByStatementId(dto.getId());
        entity = actOfDelineationRenewalMapper.toEntity(entity, dto);

        checkState(entity, dto.getEvent());
        changeState(entity, dto.getEvent());

        log.info("AOD RENEWAL [DELETED CONSUMER SIGN]: id = [{}]", entity.getId());
        baseSave(entity);
    }

    private void register(ActOfDelineationRenewalDto dto) {
        var entity = findByStatementId(dto.getId());
        entity = actOfDelineationRenewalMapper.toEntity(entity, dto);

        checkState(entity, dto.getEvent());

        entity.setOffHours(isFalse(calendarEventApiService.isProviderWorking(entity.getProviderId())));

        changeState(entity, dto.getEvent());
        entity = baseSave(entity);

        log.info("AOD RENEWAL [REGISTERED]: id = [{}]", entity.getId());
        updateData(entity, dto.getEvent());
    }

    // ─── ASSIGN ────────────────────────────────────────────────────────────

    @Override
    public void assign(UUID statementId, AssignDto dto) {
        var entity = findByStatementId(statementId);

        checkState(entity, dto.getEvent());

        var currentUser = currentUserApiService.getCurrentUser();
        List<UUID> assignees;

        if (Event.ASSIGN_TO_DIVISION_WITH_ADDRESS == dto.getEvent()
                || Event.ASSIGN_TO_EXECUTOR_WITH_ADDRESS == dto.getEvent()) {
            // Per-address: создаёт decisions сам decisionService
            decisionService.assign(entity.getId(), dto);

            assignees = decisionService.findAllByRenewalId(entity.getId()).stream()
                    .filter(d -> d.getAssignees() != null)
                    .flatMap(d -> d.getAssignees().stream())
                    .distinct()
                    .collect(Collectors.toList());
        } else if (Event.ASSIGN_TO_DIVISION == dto.getEvent()) {
            if (isEmpty(dto.getDivisions())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            }
            SubdivisionDto subdivision = providerApiService.getSubdivision(dto.getDivisions().get(0));
            entity.setSubdivision(externalSubdivisionMapper.toEntity(subdivision));

            List<UserDto> users = userApiService.searchAsList(UserFilterDto.builder()
                    .subDivisionIds(dto.getDivisions())
                    .permissions(List.of(Event.TCA_TAKE_TO_EXECUTION.name()))
                    .build());
            assignees = users.stream().map(UserDto::getId).collect(Collectors.toList());

            if (assignees.isEmpty()) {
                throw new BadRequestException(ErrorCode.NO_USERS_TO_ASSIGN.name());
            }

            // По умолчанию все адреса получают этих же исполнителей
            decisionService.bulkAssignAllAddresses(entity.getId(), assignees, subdivision);
        } else if (Event.ASSIGN_TO_EXECUTOR == dto.getEvent()) {
            if (isEmpty(dto.getExecutors())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            }
            assignees = new ArrayList<>(dto.getExecutors());
            UserDto userDto = userApiService.getUserById(dto.getExecutors().get(0));
            SubdivisionDto subdivisionDto = providerApiService.getSubdivision(userDto.getSubdivisionId());
            entity.setSubdivision(externalSubdivisionMapper.toEntity(subdivisionDto));

            decisionService.bulkAssignAllAddresses(entity.getId(), assignees, subdivisionDto);
        } else {
            throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
        }

        if (assignees.isEmpty()) {
            log.error("ERROR ACT OF DELINEATION RENEWAL [ASSIGNING]: id = [{}], error = [{}]", entity.getId(), ErrorCode.NO_USERS_TO_ASSIGN.name());
            throw new BadRequestException(ErrorCode.NO_USERS_TO_ASSIGN.name());
        }

        Set<UUID> related = new HashSet<>(
                Optional.ofNullable(entity.getRelatedUsers()).orElse(new ArrayList<>()));
        related.addAll(assignees);

        entity.setAssignees(new ArrayList<>(assignees));
        entity.setRelatedUsers(new ArrayList<>(related));
        entity.setAssignedBy(currentUser.getId());
        entity.setCurrentUserId(currentUser.getId());

        changeState(entity, dto.getEvent());
        entity = baseSave(entity);
        updateData(entity, dto.getEvent());

        log.info("AOD RENEWAL [ASSIGNED]: id = [{}], event = [{}], executors = [{}], divisions = [{}]",
                entity.getId(), dto.getEvent(), dto.getExecutors(), dto.getDivisions());
    }

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

    private void updateData(ActOfDelineationRenewalEntity entity, Event event) {
        var dto = (ActOfDelineationRenewalDto) registryApiService.getByStatementId(entity.getStatementId());
        dto = actOfDelineationRenewalMapper.toStatementDto(dto, entity);
        dto.setEvent(event);
        log.info("AOD RENEWAL [UPDATE REGISTRY DATA]: event = [{}]", event);
        registryApiService.updateData(dto.getId(), dto);
    }

    private void checkState(ActOfDelineationRenewalEntity entity, Event event) {
        log.info("AOD RENEWAL [CHECK-STATE]: status = [{}], event = [{}]", entity.getStatusCode(), event);
        try {
            actOfDelineationRenewalStatemachine.checkState(entity, null, entity.getStatusCode(), event);
        } catch (UnknownStateException | UnknownEventException | GuardException e) {
            log.error("AOD RENEWAL [CHECK-STATE]: error message = {}", e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }

    private void changeState(ActOfDelineationRenewalEntity entity, Event event) {
        log.info("AOD RENEWAL [CHANGE-STATE]: status = [{}], event = [{}]", entity.getStatusCode(), event);
        try {
            actOfDelineationRenewalStatemachine.changeState(entity, null, entity.getStatusCode(), event);
        } catch (UnknownStateException | UnknownEventException | GuardException e) {
            log.error("AOD RENEWAL [CHANGE-STATE]: error message = {}", e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }
}
