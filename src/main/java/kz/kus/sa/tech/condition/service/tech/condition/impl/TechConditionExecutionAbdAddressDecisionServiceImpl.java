package kz.kus.sa.tech.condition.service.tech.condition.impl;

import kz.kus.commons.enums.ConsumerType;
import kz.kus.sa.auth.api.currentuser.CurrentUserApiService;
import kz.kus.sa.auth.api.currentuser.dto.CurrentUserResponse;
import kz.kus.sa.auth.api.provider.ProviderApiService;
import kz.kus.sa.auth.api.provider.dto.EntryDto;
import kz.kus.sa.auth.api.provider.dto.ProviderDto;
import kz.kus.sa.auth.api.provider.enums.ActivityType;
import kz.kus.sa.consumer.api.ConsumerApiService;
import kz.kus.sa.registry.api.RegistryGenerateNumberApiService;
import kz.kus.sa.registry.api.RegistrySignApiService;
import kz.kus.sa.registry.dto.common.SignCreateDto;
import kz.kus.sa.registry.enums.Event;
import kz.kus.sa.registry.enums.RegistrationNumberDecisionType;
import kz.kus.sa.registry.enums.RegistrationNumberServiceType;
import kz.kus.sa.registry.enums.Status;
import kz.kus.sa.report.api.ReportTechConditionApiService;
import kz.kus.sa.tech.condition.dao.entity.*;
import kz.kus.sa.tech.condition.dao.mapper.ExternalUserMapper;
import kz.kus.sa.tech.condition.dao.mapper.TechConditionProjectMapper;
import kz.kus.sa.tech.condition.dao.repository.AbdAddressRepository;
import kz.kus.sa.tech.condition.dao.repository.TechConditionExecutionAbdAddressDecisionRepository;
import kz.kus.sa.tech.condition.dao.repository.TechConditionExecutionRepository;
import kz.kus.sa.tech.condition.dao.repository.TechConditionRepository;
import kz.kus.sa.tech.condition.dto.TechConditionExecuteDto;
import kz.kus.sa.tech.condition.dto.execution.TechConditionExecutionAbdAddressDecisionDto;
import kz.kus.sa.tech.condition.dto.project.TechConditionProjectCreateDto;
import kz.kus.sa.tech.condition.dto.project.TechConditionProjectDto;
import kz.kus.sa.tech.condition.dto.report.TechConditionDecisionReportDto;
import kz.kus.sa.tech.condition.enums.ExecutionStatus;
import kz.kus.sa.tech.condition.enums.ProjectStatus;
import kz.kus.sa.tech.condition.exception.BadRequestException;
import kz.kus.sa.tech.condition.exception.BusinessException;
import kz.kus.sa.tech.condition.exception.ErrorCode;
import kz.kus.sa.tech.condition.exception.NotFoundException;
import kz.kus.sa.tech.condition.service.intagration.KzharyqTechConditionService;
import kz.kus.sa.tech.condition.service.notification.NotificationService;
import kz.kus.sa.tech.condition.service.report.TechConditionReportService;
import kz.kus.sa.tech.condition.service.tech.condition.TechConditionExecutionAbdAddressDecisionService;
import kz.kus.sa.tech.condition.service.tech.condition.TechConditionProjectService;
import kz.kus.sa.tech.condition.statemachine.TechConditionExecutionStatemachine;
import kz.kus.sa.tech.condition.statemachine.TechConditionStatemachine;
import kz.kus.sa.tech.condition.statemachine.exception.GuardException;
import kz.kus.sa.tech.condition.statemachine.exception.UnknownEventException;
import kz.kus.sa.tech.condition.statemachine.exception.UnknownStateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static kz.kus.sa.registry.enums.TechConditionExecutionDecisionType.REASONED_REFUSAL;
import static kz.kus.sa.registry.enums.TechConditionExecutionDecisionType.TECHNICAL_RECOMMENDATION;
import static kz.kus.sa.tech.condition.util.CommonUtils.isNullOrEmpty;
import static kz.kus.sa.tech.condition.util.CommonUtils.stringOrEmpty;
import static kz.kus.sa.tech.condition.util.Messages.REASONED_REFUSAL_SIGNED;
import static kz.kus.sa.tech.condition.util.Messages.TECH_RECOMMENDATION_SIGNED;
import static org.hibernate.internal.util.collections.CollectionHelper.isEmpty;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TechConditionExecutionAbdAddressDecisionServiceImpl implements TechConditionExecutionAbdAddressDecisionService {

    private final TechConditionExecutionAbdAddressDecisionRepository abdAddressDecisionRepository;
    private final TechConditionExecutionRepository executionRepository;
    private final TechConditionRepository techConditionRepository;
    private final TechConditionProjectService techConditionProjectService;
    private final ProviderApiService providerApiService;
    private final RegistryGenerateNumberApiService registryGenerateNumberApiService;
    private final CurrentUserApiService currentUserApiService;
    private final ExternalUserMapper externalUserMapper;
    private final AbdAddressRepository abdAddressRepository;
    private final TechConditionExecutionStatemachine executionStatemachine;
    private final TechConditionStatemachine techConditionStatemachine;
    private final KzharyqTechConditionService kzharyqTechConditionService;
    private final RegistrySignApiService registrySignApiService;
    private final TechConditionProjectMapper techConditionProjectMapper;
    private final ReportTechConditionApiService reportTechConditionApiService;
    private final TechConditionReportService techConditionReportService;
    private final ConsumerApiService consumerApiService;
    private final NotificationService notificationService;

    @Override
    public void saveAll(UUID executionId, List<TechConditionExecutionAbdAddressDecisionDto> dtoList) {
        if (isEmpty(dtoList)) return;

        var execution = findExecutionById(executionId);

        dtoList.forEach(dto -> {
            TechConditionExecutionAbdAddressDecisionEntity entity = new TechConditionExecutionAbdAddressDecisionEntity();
            entity.setTechConditionExecution(execution);
            entity.setObjectAbdAddress(findAddressById(dto.getAbdAddressId()));
            mapDtoToEntity(dto, entity);
            abdAddressDecisionRepository.save(entity);
        });
    }

    @Override
    public void replaceAll(UUID executionId, List<TechConditionExecutionAbdAddressDecisionDto> dtoList) {
        deleteAllByExecutionId(executionId);
        saveAll(executionId, dtoList);
    }

    @Override
    public List<TechConditionExecutionAbdAddressDecisionEntity> findAllByExecutionId(UUID executionId) {
        return abdAddressDecisionRepository.findAllByTechConditionExecutionId(executionId);
    }

    @Override
    public TechConditionExecutionAbdAddressDecisionEntity findByExecutionIdAndAbdAddressId(UUID executionId, UUID abdAddressId) {
        return abdAddressDecisionRepository.findByTechConditionExecutionIdAndObjectAbdAddressId(executionId, abdAddressId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    @Override
    public void deleteAllByExecutionId(UUID executionId) {
        List<TechConditionExecutionAbdAddressDecisionEntity> existing = findAllByExecutionId(executionId);
        if (!existing.isEmpty()) {
            abdAddressDecisionRepository.deleteAll(existing);
        }
    }

    // ─── EXECUTE APPLICATION ───────────────────────────────────────────────

    @Override
    public void executeApplication(UUID executionId, TechConditionExecuteDto dto) {
        var execution = findExecutionById(executionId);
        var techCondition = execution.getTechCondition();

        validateExecuteDto(dto);

        var currentUser = currentUserApiService.getCurrentUser();
        execution.setExecutor(externalUserMapper.fromCurrentUserResponse(currentUser));
        execution.setExecutedDatetime(OffsetDateTime.now());

        List<TechConditionExecutionAbdAddressDecisionDto> addressDecisions = dto.getAbdAddressDecisions();

        if (!isEmpty(addressDecisions)) {
            // Режим: решение по каждому адресу отдельно
            processAddressDecisions(execution, techCondition, addressDecisions);
        } else {
            // Режим: одно решение на весь execution (обратная совместимость)
            processSingleDecision(execution, techCondition, dto);
        }

        executionChangeState(techCondition, execution, Event.EXECUTE);

        log.info("TECH CONDITION [EXECUTION EXECUTED]: id=[{}], executionId=[{}]", techCondition.getId(), execution.getId());
        executionRepository.save(execution);
    }

    private void processAddressDecisions(TechConditionExecutionEntity execution,
                                         TechConditionEntity techCondition,
                                         List<TechConditionExecutionAbdAddressDecisionDto> addressDecisions) {

        // Удаляем старые решения
        deleteAllByExecutionId(execution.getId());

        // Проверяем что все адреса принадлежат этому execution
        List<AbdAddressEntity> executionAddresses = abdAddressRepository.findAllByTechConditionExecutionId(execution.getId());
        Set<UUID> validAddressIds = executionAddresses.stream()
                .map(AbdAddressEntity::getId)
                .collect(Collectors.toSet());

        for (TechConditionExecutionAbdAddressDecisionDto addrDto : addressDecisions) {
            if (!validAddressIds.contains(addrDto.getAbdAddressId())) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            }
            validateAddressDecision(addrDto, execution);

            TechConditionExecutionAbdAddressDecisionEntity decision = new TechConditionExecutionAbdAddressDecisionEntity();
            decision.setTechConditionExecution(execution);
            decision.setObjectAbdAddress(findAddressById(addrDto.getAbdAddressId()));
            mapDtoToEntity(addrDto, decision);

            if (REASONED_REFUSAL == addrDto.getDecisionType()) {
                generateRefusalRegistrationNumber(decision, techCondition);
                decision.setReasonForRefusalDatetime(OffsetDateTime.now());
            }

            abdAddressDecisionRepository.save(decision);
        }
    }

    private void processSingleDecision(TechConditionExecutionEntity execution,
                                       TechConditionEntity techCondition,
                                       TechConditionExecuteDto dto) {

        // Старая логика — одно решение на весь execution
        // Создаём по одному Decision на каждый адрес
        List<AbdAddressEntity> addresses = abdAddressRepository.findAllByTechConditionExecutionId(execution.getId());

        deleteAllByExecutionId(execution.getId());

        for (AbdAddressEntity address : addresses) {
            TechConditionExecutionAbdAddressDecisionEntity decision = new TechConditionExecutionAbdAddressDecisionEntity();
            decision.setTechConditionExecution(execution);
            decision.setObjectAbdAddress(address);
            decision.setDecisionType(dto.getDecisionType());

            if (TECHNICAL_RECOMMENDATION == dto.getDecisionType()) {
                decision.setConnectionPoints(dto.getConnectionPoints());
                decision.setConsider(dto.getConsider());
                decision.setMeteringPointCode(dto.getMeteringPointCode());
                decision.setInstalledTransformer(dto.getInstalledTransformer());
                decision.setMaximumTransformerLoad(dto.getMaximumTransformerLoad());
                decision.setExistsPlaceInstallMeteringDevice(dto.getExistsPlaceInstallMeteringDevice());
                decision.setConnectionPointVoltage(dto.getConnectionPointVoltage());
                decision.setConnectionPointVoltageLevel(dto.getConnectionPointVoltageLevel());
                decision.setRequiredForConnection(dto.getRequiredForConnection());
                decision.setRequirementsForOrganizationElectricityMetering(dto.getRequirementsForOrganizationElectricityMetering());
            } else if (REASONED_REFUSAL == dto.getDecisionType()) {
                decision.setRefusalReasonCode(dto.getRefusalReasonCode());
                decision.setReasonForRefusalRu(dto.getReasonForRefusalRu());
                decision.setReasonForRefusalKk(dto.getReasonForRefusalKk());
                decision.setRefusalNumber(dto.getRefusalNumber());
                decision.setReasonForRefusalDatetime(OffsetDateTime.now());
                generateRefusalRegistrationNumber(decision, techCondition);
            }

            abdAddressDecisionRepository.save(decision);
        }
    }

    // ─── CREATE PROJECT ────────────────────────────────────────────────────

    @Override
    public TechConditionProjectDto createProject(UUID executionId, TechConditionProjectCreateDto dto) {
        TechConditionExecutionEntity execution = findExecutionById(executionId);
        TechConditionEntity techCondition = execution.getTechCondition();

        // Проверяем что по данному адресу (abdAddressId в dto)
        // есть decision с типом TECHNICAL_RECOMMENDATION
        UUID abdAddressId = dto.getAbdAddressId(); // НОВОЕ поле в CreateDto

        if (abdAddressId != null) {
            // Режим: проект для конкретного адреса
            return createProjectForAddress(execution, techCondition, dto, abdAddressId);
        } else {
            // Старый режим: один проект на весь execution
            return createProjectForExecution(execution, techCondition, dto);
        }
    }

    private TechConditionProjectDto createProjectForAddress(TechConditionExecutionEntity execution,
                                                            TechConditionEntity techCondition,
                                                            TechConditionProjectCreateDto dto,
                                                            UUID abdAddressId) {
        // Находим decision по адресу
        TechConditionExecutionAbdAddressDecisionEntity decision = abdAddressDecisionRepository
                .findByTechConditionExecutionIdAndObjectAbdAddressId(execution.getId(), abdAddressId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));

        if (decision.getDecisionType() != TECHNICAL_RECOMMENDATION) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
        }
        if (decision.getProject() != null) {
            throw new BadRequestException("Project already created for this address");
        }

        checkState(techCondition, Event.TC_FORMATION_PROJECT);

        // Создаём проект, привязанный к адресу
        TechConditionProjectEntity project = techConditionProjectService.create(techCondition, execution, buildProjectEntity(dto, decision));

        TechConditionProjectEntity registeredProject = techConditionProjectService.generateRegistrationNumber(project);

        // Привязываем проект к decision (не к execution напрямую)
        decision.setProject(registeredProject);
        abdAddressDecisionRepository.save(decision);

        // Проверяем: если ВСЕ адреса с TECHNICAL_RECOMMENDATION имеют проект
        // → меняем статус
        checkAndChangeFormationProjectState(techCondition, execution);

        log.info("TECH CONDITION [PROJECT CREATED FOR ADDRESS]: executionId=[{}], addressId=[{}], projectId=[{}]",
                execution.getId(), abdAddressId, registeredProject.getId());

        return techConditionProjectMapper.toDto(registeredProject);
    }

    private TechConditionProjectDto createProjectForExecution(TechConditionExecutionEntity execution,
                                                              TechConditionEntity techCondition,
                                                              TechConditionProjectCreateDto dto) {
        checkState(techCondition, Event.TC_FORMATION_PROJECT);

        TechConditionProjectDto project = techConditionProjectService.create(techCondition, execution, dto);
        TechConditionProjectDto registeredProject = techConditionProjectService.generateRegistrationNumber(project);

        changeState(techCondition, Event.TC_FORMATION_PROJECT);

        log.info("TECH CONDITION [PROJECT CREATED FOR EXECUTION]: executionId=[{}], projectId=[{}]",
                execution.getId(), registeredProject.getId());

        return registeredProject;
    }

    private TechConditionProjectEntity buildProjectEntity(TechConditionProjectCreateDto dto,
                                                          TechConditionExecutionAbdAddressDecisionEntity decision) {

        TechConditionProjectEntity entity = techConditionProjectMapper.toEntity(dto);

        // Заполняем из decision, если в dto не пришло
        AbdAddressEntity address = decision.getObjectAbdAddress();
        TechConditionEntity techCondition = decision.getTechConditionExecution().getTechCondition();

        // Данные из заявления
        if (isNull(entity.getConsumerType())) {
            entity.setConsumerType(techCondition.getConsumerType());
        }
        if (isNull(entity.getConsumerIinBin())) {
            entity.setConsumerIinBin(techCondition.getConsumerIinBin());
        }
        if (isNull(entity.getConsumerFullNameRu())) {
            entity.setConsumerFullNameRu(techCondition.getConsumerFullNameRu());
        }
        if (isNull(entity.getConsumerFullNameKk())) {
            entity.setConsumerFullNameKk(techCondition.getConsumerFullNameKk());
        }
        if (isNull(entity.getRequiredPower())) {
            entity.setRequiredPower(techCondition.getRequiredPower());
        }
        if (isNull(entity.getVoltageLevelCode())) {
            entity.setVoltageLevelCode(techCondition.getVoltageLevelCode());
        }
        if (isNull(entity.getConsumptionTypeCode())) {
            entity.setConsumptionTypeCode(techCondition.getConsumptionTypeCode());
        }
        if (isNull(entity.getElectricalLoadTypeCode())) {
            entity.setElectricalLoadTypeCode(techCondition.getElectricalLoadTypeCode());
        }
        if (isNull(entity.getServiceTypeCode())) {
            entity.setServiceTypeCode(techCondition.getServiceTypeCode());
        }
        if (isNull(entity.getTechConditionReasonCode())) {
            entity.setTechConditionReasonCode(techCondition.getTechConditionReasonCode());
        }

        // Данные из адреса
        if (isNull(entity.getConnectionPoint()) && StringUtils.isNotEmpty(address.getConnectionPoints())) {
            entity.setConnectionPoint(address.getConnectionPoints());
        }
        if (isNull(entity.getConnectionPointCode())) {
            entity.setConnectionPointCode(techCondition.getConnectionPointCode());
        }

        // Данные из decision (приоритет — они более специфичны, чем заявление)
        if (StringUtils.isNotEmpty(decision.getConnectionPoints())) {
            entity.setConnectionPointsRu(decision.getConnectionPoints());
            entity.setConnectionPointsKk(decision.getConnectionPoints());
        }
        if (nonNull(decision.getMeteringPointCode())) {
            entity.setMeteringPointCode(decision.getMeteringPointCode());
        }

        // Адреса объектов — только адрес из decision
        entity.setObjectAbdAddresses(List.of(address));

        return entity;
    }

    private void checkAndChangeFormationProjectState(TechConditionEntity techCondition, TechConditionExecutionEntity execution) {
        List<TechConditionExecutionAbdAddressDecisionEntity> allDecisions = findAllByExecutionId(execution.getId());

        boolean allProjectsCreated = allDecisions.stream()
                .filter(d -> d.getDecisionType() == TECHNICAL_RECOMMENDATION)
                .allMatch(d -> d.getProject() != null);

        if (allProjectsCreated && !allDecisions.isEmpty()) {
            changeState(techCondition, Event.TC_FORMATION_PROJECT);
        }
    }

    // ─── FORMATION REASONED REFUSAL ────────────────────────────────────────

    @Override
    public void formationReasonedRefusal(UUID executionId, TechConditionExecuteDto dto) {
        if (dto.getDecisionType() != REASONED_REFUSAL) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
        }

        TechConditionExecutionEntity execution = findExecutionById(executionId);
        TechConditionEntity techCondition = execution.getTechCondition();

        if (isNull(techCondition.getExecutor())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED.name());
        }

        UUID abdAddressId = dto.getAbdAddressId(); // НОВОЕ поле

        if (abdAddressId != null) {
            // Режим: мотивированный отказ по конкретному адресу
            formationReasonedRefusalForAddress(execution, techCondition, dto, abdAddressId);
        } else {
            // Старый режим: мотивированный отказ на весь execution
            formationReasonedRefusalForExecution(execution, techCondition, dto);
        }
    }

    private void formationReasonedRefusalForAddress(TechConditionExecutionEntity execution,
                                                    TechConditionEntity techCondition,
                                                    TechConditionExecuteDto dto,
                                                    UUID abdAddressId) {
        TechConditionExecutionAbdAddressDecisionEntity decision = abdAddressDecisionRepository
                .findByTechConditionExecutionIdAndObjectAbdAddressId(execution.getId(), abdAddressId)
                .orElseGet(() -> {
                    // Decision ещё не создан — создаём
                    AbdAddressEntity address = findAddressById(abdAddressId);
                    TechConditionExecutionAbdAddressDecisionEntity newDecision =
                            new TechConditionExecutionAbdAddressDecisionEntity();
                    newDecision.setTechConditionExecution(execution);
                    newDecision.setObjectAbdAddress(address);
                    return newDecision;
                });

        checkState(techCondition, Event.TC_FORMATION_REASONED_REFUSAL);

        decision.setDecisionType(REASONED_REFUSAL);
        decision.setRefusalReasonCode(dto.getRefusalReasonCode());
        decision.setReasonForRefusalRu(dto.getReasonForRefusalRu());
        decision.setReasonForRefusalKk(dto.getReasonForRefusalKk());
        decision.setRefusalNumber(dto.getRefusalNumber());
        decision.setReasonForRefusalDatetime(OffsetDateTime.now());

        generateRefusalRegistrationNumber(decision, techCondition);

        abdAddressDecisionRepository.save(decision);

        // Проверяем: если ВСЕ адреса с REASONED_REFUSAL заполнены
        checkAndChangeFormationRefusalState(techCondition, execution);

        log.info("TECH CONDITION [REASONED REFUSAL FORMED FOR ADDRESS]: executionId=[{}], addressId=[{}], regNumber=[{}]",
                execution.getId(), abdAddressId, decision.getReasonForRefusalRegistrationNumber());
    }

    private void formationReasonedRefusalForExecution(TechConditionExecutionEntity execution,
                                                      TechConditionEntity techCondition,
                                                      TechConditionExecuteDto dto) {
        checkState(techCondition, Event.TC_FORMATION_REASONED_REFUSAL);

        List<AbdAddressEntity> addresses = abdAddressRepository.findAllByTechConditionExecutionId(execution.getId());

        for (AbdAddressEntity address : addresses) {
            TechConditionExecutionAbdAddressDecisionEntity decision = abdAddressDecisionRepository
                    .findByTechConditionExecutionIdAndObjectAbdAddressId(execution.getId(), address.getId())
                    .orElseGet(() -> {
                        TechConditionExecutionAbdAddressDecisionEntity d = new TechConditionExecutionAbdAddressDecisionEntity();
                        d.setTechConditionExecution(execution);
                        d.setObjectAbdAddress(address);
                        return d;
                    });

            if (StringUtils.isEmpty(decision.getReasonForRefusalRegistrationNumber())) {
                decision.setDecisionType(REASONED_REFUSAL);
                decision.setRefusalReasonCode(dto.getRefusalReasonCode());
                decision.setReasonForRefusalRu(dto.getReasonForRefusalRu());
                decision.setReasonForRefusalKk(dto.getReasonForRefusalKk());
                decision.setRefusalNumber(dto.getRefusalNumber());
                decision.setReasonForRefusalDatetime(OffsetDateTime.now());
                generateRefusalRegistrationNumber(decision, techCondition);
                abdAddressDecisionRepository.save(decision);
            }
        }

        changeState(techCondition, Event.TC_FORMATION_REASONED_REFUSAL);
        techConditionRepository.save(techCondition);

        log.info("TECH CONDITION [REASONED REFUSAL FORMED FOR EXECUTION]: executionId=[{}]", execution.getId());
    }

    private void checkAndChangeFormationRefusalState(TechConditionEntity techCondition, TechConditionExecutionEntity execution) {
        List<AbdAddressEntity> allAddresses = abdAddressRepository.findAllByTechConditionExecutionId(execution.getId());

        List<TechConditionExecutionAbdAddressDecisionEntity> decisions = findAllByExecutionId(execution.getId());

        Set<UUID> refusalAddressIds = decisions.stream()
                .filter(d -> d.getDecisionType() == REASONED_REFUSAL
                        && StringUtils.isNotEmpty(d.getReasonForRefusalRegistrationNumber()))
                .map(d -> d.getObjectAbdAddress().getId())
                .collect(Collectors.toSet());

        boolean allFormed = allAddresses.stream()
                .allMatch(a -> refusalAddressIds.contains(a.getId()));

        if (allFormed) {
            changeState(techCondition, Event.TC_FORMATION_REASONED_REFUSAL);
            techConditionRepository.save(techCondition);
        }
    }

    // ─── SIGN DECISION ─────────────────────────────────────────────────────

    @Override
    public void signDecision(UUID executionId, SignCreateDto sign) {
        TechConditionExecutionEntity execution = findExecutionById(executionId);
        TechConditionEntity techCondition = execution.getTechCondition();

        executionCheckState(techCondition, execution, Event.TC_SIGN);

        // Проверяем что у всех адресов есть решения
        validateAllAddressesHaveDecisions(execution);

        CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
        techCondition.setDirector(externalUserMapper.fromCurrentUserResponse(currentUser));
        techCondition.setDirectorSignedDatetime(OffsetDateTime.now());

        // Подписываем — решение может быть смешанным (часть адресов ТР, часть МО)
        // Поэтому генерируем один отчёт с агрегацией всех решений
        TechConditionDecisionReportDto decisionReportData = techConditionReportService.getDecisionReportData(executionId);
        sign.setBase64Data(reportTechConditionApiService.decisionBase64FromDto(executionId, decisionReportData, null));
        registrySignApiService.addProviderSign(techCondition.getStatementId(), sign);

        executionChangeState(techCondition, execution, Event.TC_SIGN);

        // Обновляем статусы проектов
        List<TechConditionExecutionAbdAddressDecisionEntity> decisions = findAllByExecutionId(execution.getId());
        decisions.forEach(d -> {
            if (d.getDecisionType() == TECHNICAL_RECOMMENDATION && d.getProject() != null) {
                d.getProject().setStatusCode(ProjectStatus.SIGNED.getCode());
            }
        });
        abdAddressDecisionRepository.saveAll(decisions);

        // Все ли executions подписаны?
        boolean allSigned = !executionRepository.existsByTechConditionIdAndDeletedDatetimeIsNullAndStatusCodeIsNot(
                techCondition.getId(), ExecutionStatus.SIGNED.getCode());
        if (allSigned) {
            techCondition.setStatusCode(Status.COMPLETED.getCode());
        }

        techConditionRepository.save(techCondition);
        executionRepository.save(execution);

        log.info("TECH CONDITION [DECISION SIGNED]: id=[{}], executionId=[{}]", techCondition.getId(), execution.getId());

        sendNotifications(techCondition, decisions);
        kzharyqTechConditionService.sendCompletedRequest(techCondition);
    }

    // ─── ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ────────────────────────────────────────────

    private void generateRefusalRegistrationNumber(TechConditionExecutionAbdAddressDecisionEntity decision,
                                                   TechConditionEntity techCondition) {
        if (StringUtils.isNotEmpty(decision.getReasonForRefusalRegistrationNumber())) {
            return; // уже сгенерирован
        }

        ProviderDto providerDto = providerApiService.getProviderDto(techCondition.getProviderId());

        Long nextNumber = executionRepository.getMaxReasonForRefusalInternalRegistrationNumberByOrganizationIdAndYear(
                providerDto.getId(), OffsetDateTime.now().getYear()) + 1;

        String activityTypeCodes = resolveActivityTypeCodes(providerDto);

        decision.setReasonForRefusalInternalRegistrationNumber(nextNumber);
        decision.setReasonForRefusalRegistrationNumber(
                registryGenerateNumberApiService.generateDecisionNumber(
                        providerDto.getCode(),
                        RegistrationNumberServiceType.SA,
                        RegistrationNumberDecisionType.M,
                        activityTypeCodes,
                        nextNumber,
                        null
                )
        );
    }

    private String resolveActivityTypeCodes(ProviderDto providerDto) {
        if (!isNullOrEmpty(providerDto.getActivityTypeCodes())) {
            return providerDto.getActivityTypeCodes().stream()
                    .map(code -> String.valueOf(ActivityType.findByCode(code)))
                    .distinct().sorted().collect(Collectors.joining());
        }
        return providerDto.getActivityTypes().stream()
                .map(EntryDto::getCode)
                .distinct().sorted().collect(Collectors.joining());
    }

    private void validateExecuteDto(TechConditionExecuteDto dto) {
        boolean hasAddressDecisions = !isEmpty(dto.getAbdAddressDecisions());
        boolean hasSingleDecision = nonNull(dto.getDecisionType());

        if (!hasAddressDecisions && !hasSingleDecision) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
        }
        if (hasAddressDecisions && hasSingleDecision) {
            throw new BadRequestException("Specify either decisionType or addressDecisions, not both");
        }
    }

    private void validateAddressDecision(TechConditionExecutionAbdAddressDecisionDto dto, TechConditionExecutionEntity execution) {
        if (isNull(dto.getDecisionType())) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
        }

        if (TECHNICAL_RECOMMENDATION == dto.getDecisionType()) {
            if (Boolean.TRUE.equals(execution.getIsParallel())) {
                if (StringUtils.isEmpty(dto.getConsider())) {
                    throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
                }
            } else {
                if (StringUtils.isEmpty(dto.getConnectionPoints())
                        || StringUtils.isEmpty(dto.getConsider())) {
                    throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
                }
            }
        }

        if (REASONED_REFUSAL == dto.getDecisionType()) {
            if (StringUtils.isEmpty(dto.getRefusalReasonCode())
                    && (StringUtils.isEmpty(dto.getReasonForRefusalRu())
                    || StringUtils.isEmpty(dto.getReasonForRefusalKk()))) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            }
        }
    }

    private void validateAllAddressesHaveDecisions(TechConditionExecutionEntity execution) {
        List<AbdAddressEntity> addresses = abdAddressRepository.findAllByTechConditionExecutionId(execution.getId());
        List<TechConditionExecutionAbdAddressDecisionEntity> decisions = findAllByExecutionId(execution.getId());

        Set<UUID> decidedAddressIds = decisions.stream()
                .map(d -> d.getObjectAbdAddress().getId())
                .collect(Collectors.toSet());

        boolean allHaveDecisions = addresses.stream()
                .allMatch(a -> decidedAddressIds.contains(a.getId()));

        if (!allHaveDecisions) {
            throw new BusinessException(ErrorCode.DECISION_NOT_FORMED.name());
        }
    }

    private void mapDtoToEntity(TechConditionExecutionAbdAddressDecisionDto dto,
                                TechConditionExecutionAbdAddressDecisionEntity entity) {
        entity.setDecisionType(dto.getDecisionType());

        if (TECHNICAL_RECOMMENDATION == dto.getDecisionType()) {
            entity.setConnectionPoints(dto.getConnectionPoints());
            entity.setMeteringPointCode(dto.getMeteringPointCode());
            entity.setConsider(dto.getConsider());
            entity.setInstalledTransformer(dto.getInstalledTransformer());
            entity.setMaximumTransformerLoad(dto.getMaximumTransformerLoad());
            entity.setExistsPlaceInstallMeteringDevice(dto.getExistsPlaceInstallMeteringDevice());
            entity.setConnectionPointVoltage(dto.getConnectionPointVoltage());
            entity.setConnectionPointVoltageLevel(dto.getConnectionPointVoltageLevel());
            entity.setRequiredForConnection(dto.getRequiredForConnection());
            entity.setRequirementsForOrganizationElectricityMetering(dto.getRequirementsForOrganizationElectricityMetering());
        } else if (REASONED_REFUSAL == dto.getDecisionType()) {
            entity.setRefusalReasonCode(dto.getRefusalReasonCode());
            entity.setReasonForRefusalRu(dto.getReasonForRefusalRu());
            entity.setReasonForRefusalKk(dto.getReasonForRefusalKk());
            entity.setRefusalNumber(dto.getRefusalNumber());
        }
    }

    private void sendNotifications(TechConditionEntity techCondition,
                                   List<TechConditionExecutionAbdAddressDecisionEntity> decisions) {
        // Определяем итоговый тип решения для уведомления
        // Если есть хотя бы одна ТР — уведомляем как ТР
        // Если все МО — уведомляем как МО
        boolean hasTechRecommendation = decisions.stream()
                .anyMatch(d -> d.getDecisionType() == TECHNICAL_RECOMMENDATION);
        boolean hasReasonedRefusal = decisions.stream()
                .anyMatch(d -> d.getDecisionType() == REASONED_REFUSAL);

        try {
            ProviderDto provider = providerApiService.getProviderDto(techCondition.getProviderId());

            var consumer = consumerApiService.getConsumer(
                    techCondition.getConsumerIinBin(),
                    provider.getIinBin(),
                    ConsumerType.valueOf(techCondition.getConsumerType().name()));

            if (isNull(consumer) || isNull(consumer.getEmail())) {
                log.error("TECH CONDITION EXECUTION Consumer [{},{},{}] not found or email is null",
                        techCondition.getConsumerIinBin(),
                        provider.getIinBin(),
                        techCondition.getConsumerType());
                return;
            }

            String executorFullName = nonNull(techCondition.getExecutor())
                    ? stringOrEmpty(techCondition.getExecutor().getFullName())
                    : "";
            String providerContact = stringOrEmpty(provider.getContact());

            if (hasTechRecommendation && !hasReasonedRefusal) {
                // Только ТР
                notificationService.send(
                        consumer.getEmail(),
                        String.format(
                                TECH_RECOMMENDATION_SIGNED,
                                stringOrEmpty(provider.getKk()),
                                stringOrEmpty(techCondition.getStatementRegistrationNumber()),
                                stringOrEmpty(provider.getRu()),
                                stringOrEmpty(techCondition.getStatementRegistrationNumber()),
                                executorFullName,
                                providerContact
                        )
                );

            } else if (hasReasonedRefusal && !hasTechRecommendation) {
                // Только МО
                notificationService.send(
                        consumer.getEmail(),
                        String.format(
                                REASONED_REFUSAL_SIGNED,
                                stringOrEmpty(provider.getKk()),
                                stringOrEmpty(techCondition.getStatementRegistrationNumber()),
                                stringOrEmpty(provider.getRu()),
                                stringOrEmpty(techCondition.getStatementRegistrationNumber()),
                                executorFullName,
                                providerContact
                        )
                );

            } else if (hasTechRecommendation) {
                // Смешанный случай — отправляем оба уведомления
                notificationService.send(
                        consumer.getEmail(),
                        String.format(
                                TECH_RECOMMENDATION_SIGNED,
                                stringOrEmpty(provider.getKk()),
                                stringOrEmpty(techCondition.getStatementRegistrationNumber()),
                                stringOrEmpty(provider.getRu()),
                                stringOrEmpty(techCondition.getStatementRegistrationNumber()),
                                executorFullName,
                                providerContact
                        )
                );
                notificationService.send(
                        consumer.getEmail(),
                        String.format(
                                REASONED_REFUSAL_SIGNED,
                                stringOrEmpty(provider.getKk()),
                                stringOrEmpty(techCondition.getStatementRegistrationNumber()),
                                stringOrEmpty(provider.getRu()),
                                stringOrEmpty(techCondition.getStatementRegistrationNumber()),
                                executorFullName,
                                providerContact
                        )
                );
            }

        } catch (Exception e) {
            log.error("TECH CONDITION EXECUTION [SEND NOTIFICATION ERROR]: id=[{}], error=[{}]",
                    techCondition.getId(), e.getMessage(), e);
        }
    }

    private AbdAddressEntity findAddressById(UUID abdAddressId) {
        return abdAddressRepository.findById(abdAddressId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    private TechConditionExecutionEntity findExecutionById(UUID id) {
        return executionRepository.findByIdAndDeletedDatetimeIsNull(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));
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
            executionStatemachine.checkState(entity, execution, execution.getStatusCode(), event);
        } catch (UnknownStateException | UnknownEventException | GuardException e) {
            log.error("TECH CONDITION EXECUTION [CHECK-STATE]: error message = {}", e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }

    private void executionChangeState(TechConditionEntity entity, TechConditionExecutionEntity execution, Event event) {
        log.info("TECH CONDITION EXECUTION [CHANGE-STATE]: status = [{}], event = [{}]", execution.getStatusCode(), event);
        try {
            executionStatemachine.changeState(entity, execution, execution.getStatusCode(), event);
        } catch (UnknownStateException | UnknownEventException | GuardException e) {
            log.error("TECH CONDITION EXECUTION [CHANGE-STATE]: error message = {}", e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }
}
