package kz.kus.sa.tech.condition.service.tech.condition.impl;

import kz.kus.sa.auth.api.currentuser.dto.CurrentUserResponse;
import kz.kus.sa.auth.api.provider.dto.EntryDto;
import kz.kus.sa.auth.api.provider.dto.ProviderDto;
import kz.kus.sa.auth.api.provider.enums.ActivityType;
import kz.kus.sa.auth.api.user.dto.UserDto;
import kz.kus.sa.registry.dto.common.SignCreateDto;
import kz.kus.sa.registry.enums.Event;
import kz.kus.sa.registry.enums.RegistrationNumberDecisionType;
import kz.kus.sa.registry.enums.RegistrationNumberServiceType;
import kz.kus.sa.registry.enums.Status;
import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionAbdAddressDecisionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionEntity;
import kz.kus.sa.tech.condition.dao.repository.TechConditionExecutionAbdAddressDecisionRepository;
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
import kz.kus.sa.tech.condition.service.tech.condition.TechConditionExecutionAbdAddressDecisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static kz.kus.sa.registry.enums.TechConditionExecutionDecisionType.REASONED_REFUSAL;
import static kz.kus.sa.registry.enums.TechConditionExecutionDecisionType.TECHNICAL_RECOMMENDATION;
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

    @Override
    public void saveAll(UUID executionId, List<TechConditionExecutionAbdAddressDecisionDto> dtos) {

    }

    @Override
    public void replaceAll(UUID executionId, List<TechConditionExecutionAbdAddressDecisionDto> dtos) {

    }

    @Override
    public List<TechConditionExecutionAbdAddressDecisionEntity> findAllByExecutionId(UUID executionId) {
        return null;
    }

    @Override
    public TechConditionExecutionAbdAddressDecisionEntity findByExecutionIdAndAbdAddressId(UUID executionId, UUID abdAddressId) {
        return null;
    }

    @Override
    public void deleteAllByExecutionId(UUID executionId) {

    }

    @Override
    public void executeApplication(UUID id, TechConditionExecuteDto dto) {
        TechConditionExecutionEntity entity = findById(id);
        TechConditionEntity techConditionEntity = entity.getTechCondition();

        if (isNull(dto.getDecisionType())) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
        }

        CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
        entity.setExecutor(externalUserMapper.fromCurrentUserResponse(currentUser));
        entity.setExecutedDatetime(OffsetDateTime.now());

        if (TECHNICAL_RECOMMENDATION == dto.getDecisionType()) {
            if (Boolean.TRUE.equals(entity.getIsParallel())) {
                if (StringUtils.isEmpty(dto.getConsider())) {
                    throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
                }
            } else {
                if (StringUtils.isEmpty(dto.getConnectionPoints()) || StringUtils.isEmpty(dto.getConsider())) {
                    throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
                }
            }
            entity.setDecisionType(TECHNICAL_RECOMMENDATION);
            entity.setConnectionPoints(dto.getConnectionPoints());
            entity.setConsider(dto.getConsider());
            entity.setMeteringPointCode(dto.getMeteringPointCode());
            if (StringUtils.isNotEmpty(dto.getInstalledTransformer())) {
                entity.setInstalledTransformer(dto.getInstalledTransformer());
//                techConditionEntity.setInstalledTransformer(dto.getInstalledTransformer());
            }
            if (StringUtils.isNotEmpty(dto.getMaximumTransformerLoad())) {
                entity.setMaximumTransformerLoad(dto.getMaximumTransformerLoad());
//                techConditionEntity.setMaximumTransformerLoad(dto.getMaximumTransformerLoad());
            }
            if (nonNull(dto.getExistsPlaceInstallMeteringDevice())) {
                entity.setExistsPlaceInstallMeteringDevice(dto.getExistsPlaceInstallMeteringDevice());
//                techConditionEntity.setExistsPlaceInstallMeteringDevice(dto.getExistsPlaceInstallMeteringDevice());
            }
            if (StringUtils.isNotEmpty(dto.getConnectionPointVoltage())) {
                entity.setConnectionPointVoltage(dto.getConnectionPointVoltage());
//                techConditionEntity.setConnectionPointVoltage(dto.getConnectionPointVoltage());
            }
            if (StringUtils.isNotEmpty(dto.getConnectionPointVoltageLevel())) {
                entity.setConnectionPointVoltageLevel(dto.getConnectionPointVoltageLevel());
//                techConditionEntity.setConnectionPointVoltageLevel(dto.getConnectionPointVoltageLevel());
            }
            if (StringUtils.isNotEmpty(dto.getRequiredForConnection())) {
                entity.setRequiredForConnection(dto.getRequiredForConnection());
//                techConditionEntity.setRequiredForConnection(dto.getRequiredForConnection());
            }
            if (StringUtils.isNotEmpty(dto.getRequirementsForOrganizationElectricityMetering())) {
                entity.setRequirementsForOrganizationElectricityMetering(dto.getRequirementsForOrganizationElectricityMetering());
//                techConditionEntity.setRequirementsForOrganizationElectricityMetering(dto.getRequirementsForOrganizationElectricityMetering());
            }
        }

        boolean statusChanged = false;
        if (REASONED_REFUSAL == dto.getDecisionType()) {
            if (StringUtils.isEmpty(dto.getRefusalReasonCode()) &&
                    (StringUtils.isEmpty(dto.getReasonForRefusalRu()) ||
                            StringUtils.isEmpty(dto.getReasonForRefusalKk()))) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
            }
            entity.setRefusalReasonCode(dto.getRefusalReasonCode());
            entity.setReasonForRefusalRu(dto.getReasonForRefusalRu());
            entity.setReasonForRefusalKk(dto.getReasonForRefusalKk());
            entity.setReasonForRefusalDatetime(OffsetDateTime.now());
            entity.setDecisionType(REASONED_REFUSAL);

            if (nonNull(techConditionEntity.getExecutor()) &&
                    Objects.equals(techConditionEntity.getExecutor().getId(), currentUser.getId())) {
                List<TechConditionExecutionEntity> allExecution = findAllByTechConditionId(techConditionEntity.getId());
                if (allExecution.size() == 1) {
                    statusChanged = true;
//                    techConditionEntity.setReasonForRefusalRu(dto.getReasonForRefusalRu()); todo
//                    techConditionEntity.setReasonForRefusalKk(dto.getReasonForRefusalKk());
//                    techConditionEntity.setReasonForRefusalDatetime(OffsetDateTime.now());
//                    techConditionEntity.setDecisionType(REASONED_REFUSAL);
                    techConditionEntity.setAssignees(List.of(currentUser.getId()));
                    baseSave(techConditionEntity);
                }
            }
        }

        executionChangeState(techConditionEntity, entity, Event.EXECUTE);

        // todo DP
//        if (statusChanged) {
//            try {
//                techConditionKafkaService.createDpResponseAndSendToKafka(techConditionEntity); todo DP
//            } catch (Exception e) {
//                log.error("ERROR SENDING TO DP REGISTERED TECH CONDITION: id = [{}], registration number = [{}]",
//                        techConditionEntity.getId(), techConditionEntity.getStatementRegistrationNumber(), e);
//            }
//        }

        log.info("TECH CONDITION [EXECUTION EXECUTED]: id = [{}], execution id = [{}], decision = [{}]",
                techConditionEntity.getId(), entity.getId(), dto.getDecisionType());
        baseSave(entity);
    }

    @Override
    public void signDecision(UUID id, SignCreateDto sign) {
        TechConditionExecutionEntity entity = findById(id);
        TechConditionEntity techConditionEntity = entity.getTechCondition();

//        checkState(techConditionEntity, Event.TC_SIGN);
        executionCheckState(techConditionEntity, entity, Event.TC_SIGN);

        CurrentUserResponse currentUser = currentUserApiService.getCurrentUser();
        techConditionEntity.setDirector(externalUserMapper.fromCurrentUserResponse(currentUser));
        techConditionEntity.setDirectorSignedDatetime(OffsetDateTime.now());
        baseSave(techConditionEntity);

        TechConditionDecisionReportDto decisionReportData = reportService.getDecisionReportData(id);
        sign.setBase64Data(reportTechConditionApiService.decisionBase64FromDto(id, decisionReportData, null));
        registrySignApiService.addProviderSign(techConditionEntity.getStatementId(), sign);

//        changeState(techConditionEntity, Event.TC_SIGN);
        executionChangeState(techConditionEntity, entity, Event.TC_SIGN);

        techConditionEntity.setAssignees(List.of(techConditionEntity.getExecutor().getId()));
        entity.setAssignees(List.of(techConditionEntity.getExecutor().getId()));

        UserDto owner = userApiService.getUserById(techConditionEntity.getExecutor().getId());
        entity.setOwner(externalUserMapper.toEntity(owner));

        boolean allExecutionsIsDone = !haveNotCompletedByTechConditionId(id, ExecutionStatus.SIGNED);
        if (allExecutionsIsDone)
            techConditionEntity.setStatusCode(Status.COMPLETED.getCode());

        if (REASONED_REFUSAL == entity.getDecisionType()) {
            notificationSend(techConditionEntity, REASONED_REFUSAL_SIGNED);
        } else if (TECHNICAL_RECOMMENDATION == entity.getDecisionType()) {
            if (nonNull(entity.getProject()))
                entity.getProject().setStatusCode(ProjectStatus.SIGNED.getCode());
            notificationSend(techConditionEntity, TECH_RECOMMENDATION_SIGNED);
        }

        entity = baseSave(entity);
        techConditionEntity = baseSave(techConditionEntity);

        log.info("TECH CONDITION [EXECUTION DECISION SIGNED]: id = [{}], execution id = [{}]",
                techConditionEntity.getId(), entity.getId());

//        try {
//            techConditionKafkaService.createDpResponseAndSendToKafka(techConditionEntity); todo DP
//        } catch (Exception e) {
//            log.error("ERROR SENDING TO DP REGISTERED TECH CONDITION: id = [{}], registration number = [{}]",
//                    techConditionEntity.getId(), techConditionEntity.getStatementRegistrationNumber(), e);
//        }

        kzharyqTechConditionService.sendCompletedRequest(techConditionEntity);
    }

    @Override
    public TechConditionProjectDto createProject(UUID id, TechConditionProjectCreateDto dto) {
        TechConditionExecutionEntity entity = findById(id);
        TechConditionEntity techConditionEntity = entity.getTechCondition();

        checkState(techConditionEntity, Event.TC_FORMATION_PROJECT);

        var project = techConditionProjectService.create(techConditionEntity, entity, dto);
        var registeredProject = techConditionProjectService.generateRegistrationNumber(project);

        changeState(techConditionEntity, Event.TC_FORMATION_PROJECT);

        log.info("TECH CONDITION [EXECUTION DECISION PROJECT EXECUTED]: id = [{}], execution id = [{}], project registration number = [{}]",
                techConditionEntity.getId(), entity.getId(), registeredProject.getRegistrationNumber());
        return registeredProject;
    }

    @Override
    public void formationReasonedRefusal(UUID id, TechConditionExecuteDto dto) {
        if (dto.getDecisionType() != REASONED_REFUSAL) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
        }

        TechConditionExecutionEntity entity = findById(id);
        TechConditionEntity techConditionEntity = entity.getTechCondition();

        if (isNull(techConditionEntity.getExecutor())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED.name());
        }

        checkState(techConditionEntity, Event.TC_FORMATION_REASONED_REFUSAL);

        entity.setDecisionType(REASONED_REFUSAL);
        entity.setRefusalReasonCode(dto.getRefusalReasonCode());
        entity.setReasonForRefusalRu(dto.getReasonForRefusalRu());
        entity.setReasonForRefusalKk(dto.getReasonForRefusalKk());
        entity.setReasonForRefusalDatetime(OffsetDateTime.now());
        entity.setRefusalNumber(dto.getRefusalNumber());

        if (StringUtils.isEmpty(entity.getReasonForRefusalRegistrationNumber())) {
            ProviderDto providerDto = providerApiService.getProviderDto(techConditionEntity.getProviderId());
            Long nextNumber = techConditionExecutionRepository
                    .getMaxReasonForRefusalInternalRegistrationNumberByOrganizationIdAndYear(
                            providerDto.getId(), OffsetDateTime.now().getYear()) + 1;
            String activityTypeCodes = isEmpty(providerDto.getActivityTypeCodes())
                    ? providerDto.getActivityTypes().stream()
                      .map(EntryDto::getCode)
                      .distinct()
                      .sorted()
                      .collect(Collectors.joining())
                    : providerDto.getActivityTypeCodes().stream()
                      .map(code -> String.valueOf(ActivityType.findByCode(code)))
                      .distinct()
                      .sorted()
                      .collect(Collectors.joining());

            entity.setReasonForRefusalInternalRegistrationNumber(nextNumber);
            entity.setReasonForRefusalRegistrationNumber(
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

        // todo execution refused with out statement
        // reasoned refusal immediately issued
//        List<TechConditionExecutionEntity> executions = findAllByTechConditionId(techConditionEntity.getId());
//        if (executions.size() == 1) {
//            TechConditionExecutionEntity execution = executions.get(0);
//            if (execution.getAssignees().contains(techConditionEntity.getExecutor().getId())
//                    && Objects.equals(execution.getStatusCode(), ExecutionStatus.ON_EXECUTION.getCode())) {
//                execution.setRefusalReasonCode(dto.getRefusalReasonCode());
//                execution.setDecisionType(REASONED_REFUSAL);
//                execution.setQuickRefusal(true);
//                baseSave(execution);
//            }
//        }

        changeState(techConditionEntity, Event.TC_FORMATION_REASONED_REFUSAL);

        baseSave(entity);

//        try {
//            techConditionKafkaService.createDpResponseAndSendToKafka(techConditionEntity); todo DP
//        } catch (Exception e) {
//            log.error("ERROR SENDING TO DP REGISTERED TECH CONDITION: id = [{}], registration number = [{}]",
//                    techConditionEntity.getId(), techConditionEntity.getStatementRegistrationNumber(), e);
//        }

        log.info("TECH CONDITION [EXECUTION DECISION REASONED REFUSAL EXECUTED]: id = [{}], execution id = [{}], reasoned refusal registration number = [{}]",
                techConditionEntity.getId(), entity.getId(), entity.getReasonForRefusalRegistrationNumber());

//        return techConditionMapper.toStatementDto(baseSave(techConditionEntity));
        baseSave(techConditionEntity);
    }

    private void notificationSend(TechConditionEntity techConditionEntity, String decisionText) {
        ProviderDto provider = providerApiService.getProviderDto(techConditionEntity.getProviderId());

        var consumer = consumerApiService.getConsumer(
                techConditionEntity.getConsumerIinBin(),
                provider.getIinBin(),
                kz.kus.commons.enums.ConsumerType.valueOf(techConditionEntity.getConsumerType().name()));

        if (nonNull(consumer) && nonNull(consumer.getEmail()))
            notificationService.send(consumer.getEmail(), String.format(
                    decisionText,
                    stringOrEmpty(provider.getKk()),
                    stringOrEmpty(techConditionEntity.getStatementRegistrationNumber()),
                    stringOrEmpty(provider.getRu()),
                    stringOrEmpty(techConditionEntity.getStatementRegistrationNumber()),
                    stringOrEmpty(techConditionEntity.getExecutor().getFullName()),
                    stringOrEmpty(provider.getContact())));
        else
            log.error("TECH CONDITION EXECUTION Consumer [{},{},{}] not found or email is null",
                    techConditionEntity.getConsumerIinBin(),
                    provider.getIinBin(),
                    techConditionEntity.getConsumerType());
    }
}
