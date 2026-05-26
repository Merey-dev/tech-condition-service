package kz.kus.sa.tech.condition.service.report.impl;

import kz.kus.sa.ar.api.ArApiService;
import kz.kus.sa.auth.api.provider.ProviderApiService;
import kz.kus.sa.consumer.api.ConsumerApiService;
import kz.kus.sa.dictionary.api.DictionaryApiService;
import kz.kus.sa.fl.api.PersonApi;
import kz.kus.sa.registry.api.RegistrySignApiService;
import kz.kus.sa.registry.dto.report.v1.SignReportDto;
import kz.kus.sa.registry.dto.v1.StatementSignDto;
import kz.kus.sa.registry.enums.SignedDocType;
import kz.kus.sa.tech.condition.dao.entity.*;
import kz.kus.sa.tech.condition.dao.mapper.TechConditionEpoReportMapper;
import kz.kus.sa.tech.condition.dao.repository.TechConditionExecutionAbdAddressDecisionRepository;
import kz.kus.sa.tech.condition.dao.repository.TechConditionExecutionRepository;
import kz.kus.sa.tech.condition.dto.report.epo.TechConditionEpoApplicationReportDto;
import kz.kus.sa.tech.condition.dto.report.epo.TechConditionEpoDecisionReportDto;
import kz.kus.sa.tech.condition.exception.BadRequestException;
import kz.kus.sa.tech.condition.exception.BusinessException;
import kz.kus.sa.tech.condition.exception.ErrorCode;
import kz.kus.sa.tech.condition.exception.NotFoundException;
import kz.kus.sa.tech.condition.service.address.AbdAddressService;
import kz.kus.sa.tech.condition.service.report.CommonReportService;
import kz.kus.sa.tech.condition.service.report.TechConditionEpoReportService;
import kz.kus.sa.tech.condition.service.tech.condition.TechConditionReliabilityCategoryService;
import kz.kus.sa.tech.condition.service.tech.condition.TechConditionSubConsumerService;
import kz.kus.sa.ul.api.OrganizationApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static kz.kus.commons.enums.ConsumerType.ORGANIZATION;
import static kz.kus.sa.registry.enums.TechConditionExecutionDecisionType.REASONED_REFUSAL;
import static kz.kus.sa.registry.enums.TechConditionExecutionDecisionType.TECHNICAL_RECOMMENDATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class TechConditionEpoReportServiceImpl implements TechConditionEpoReportService {

    private final PersonApi personApi;
    private final ArApiService arApiService;
    private final OrganizationApi organizationApi;
    private final AbdAddressService abdAddressService;
    private final ConsumerApiService consumerApiService;
    private final ProviderApiService providerApiService;
    private final CommonReportService commonReportService;
    private final DictionaryApiService dictionaryApiService;
    private final RegistrySignApiService registrySignApiService;
    private final TechConditionEpoReportMapper techConditionEpoReportMapper;
    private final TechConditionSubConsumerService techConditionSubConsumerService;
    private final TechConditionExecutionRepository techConditionExecutionRepository;
    private final TechConditionReliabilityCategoryService techConditionReliabilityCategoryService;
    private final TechConditionExecutionAbdAddressDecisionRepository abdAddressDecisionRepository;

    @Override
    public TechConditionEpoApplicationReportDto applicationReportData(UUID executionId) {
        var execution = findExecutionById(executionId);
        var techCondition = execution.getTechCondition();

        var dto = techConditionEpoReportMapper.toApplicationReportDto(execution);

        this.fillCommonFields(dto, techCondition, executionId);

        HashMap<String, Object> params = new HashMap<>();
        commonReportService.setBlankHeaderInfo(techCondition.getProviderId(), params);

        dto.setParams(params);
        return dto;
    }

    @Override
    public TechConditionEpoDecisionReportDto getDecisionReportData(UUID executionId) {
        var execution = findExecutionById(executionId);
        var techCondition = execution.getTechCondition();

        var decisions = abdAddressDecisionRepository.findAllByTechConditionExecutionId(executionId);

        if (decisions.isEmpty()) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
        }

        boolean allTR = decisions.stream()
                .allMatch(d -> d.getDecisionType() == TECHNICAL_RECOMMENDATION);
        boolean allRR = decisions.stream()
                .allMatch(d -> d.getDecisionType() == REASONED_REFUSAL);

        HashMap<String, Object> params = new HashMap<>();
        commonReportService.setBlankHeaderInfo(techCondition.getProviderId(), params);

        if (allTR) {
            return buildTechRecommendationReport(executionId, techCondition, decisions, params);
        } else if (allRR) {
            return buildReasonedRefusalReport(executionId, techCondition, decisions, params);
        } else {
            long trCount = decisions.stream()
                    .filter(d -> d.getDecisionType() == TECHNICAL_RECOMMENDATION).count();
            long rrCount = decisions.stream()
                    .filter(d -> d.getDecisionType() == REASONED_REFUSAL).count();

            if (trCount >= rrCount) {
                var filterDecisions = decisions.stream()
                        .filter(d -> d.getDecisionType() == TECHNICAL_RECOMMENDATION)
                        .collect(Collectors.toList());
                return buildTechRecommendationReport(executionId, techCondition, filterDecisions, params);
            } else {
                var filterDecisions = decisions.stream()
                        .filter(d -> d.getDecisionType() == REASONED_REFUSAL)
                        .collect(Collectors.toList());
                return buildReasonedRefusalReport(executionId, techCondition, filterDecisions, params);
            }
        }
    }

    @Override
    public TechConditionEpoDecisionReportDto getDecisionReportDataByAddress(UUID executionId, UUID abdAddressId) {
        var execution = findExecutionById(executionId);
        var techCondition = execution.getTechCondition();

        var decision = abdAddressDecisionRepository
                .findByTechConditionExecutionIdAndObjectAbdAddressId(executionId, abdAddressId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));

        HashMap<String, Object> params = new HashMap<>();
        commonReportService.setBlankHeaderInfo(techCondition.getProviderId(), params);

        if (decision.getDecisionType() == TECHNICAL_RECOMMENDATION) {
            return buildTechRecommendationReport(executionId, techCondition, List.of(decision), params);
        } else if (decision.getDecisionType() == REASONED_REFUSAL) {
            return buildReasonedRefusalReport(executionId, techCondition, List.of(decision), params);
        }

        throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
    }

    // ─── BUILD TR REPORT ───────────────────────────────────────────────────

    private TechConditionEpoDecisionReportDto buildTechRecommendationReport(UUID executionId,
                                                                            TechConditionEntity techCondition,
                                                                            List<TechConditionExecutionAbdAddressDecisionEntity> decisions,
                                                                            HashMap<String, Object> params) {
        var firstDecision = decisions.stream()
                .filter(d -> nonNull(d.getProject()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.DECISION_NOT_FORMED.name()));

        var techRecommendationDto = techConditionEpoReportMapper.toTechnicalRecommendationReportDto(firstDecision);
        var dto = techConditionEpoReportMapper.toDecisionReportDto(techRecommendationDto);

        this.fillCommonFields(dto, techCondition);

        var addressData = objectAddressDataFromDecisions(decisions);
        dto.setObjectNameRu(addressData.get("objectNameRu"));
        dto.setObjectNameKk(addressData.get("objectNameKk"));
        dto.setObjectAddressRu(addressData.get("objectAddressRu"));
        dto.setObjectAddressKk(addressData.get("objectAddressKk"));

        this.setSubConsumers(techCondition.getId(), params);
        this.setReliabilityCategoriesWithKwt(techCondition.getId(), params);
        this.fillDictionaryFields(dto, techCondition);

        dto.setSigns(getSignsByIdAndDocType(techCondition.getStatementId(), Collections.singletonList(SignedDocType.TC_PROJECT)));
        dto.setDecisionType(TECHNICAL_RECOMMENDATION);
        dto.setParams(params);

        log.info("REPORT [TECHNICAL RECOMMENDATION]: executionId=[{}], tcId=[{}]", executionId, techCondition.getId());

        return dto;
    }

    // ─── BUILD RR REPORT ───────────────────────────────────────────────────

    private TechConditionEpoDecisionReportDto buildReasonedRefusalReport(UUID executionId,
                                                                         TechConditionEntity techCondition,
                                                                         List<TechConditionExecutionAbdAddressDecisionEntity> decisions,
                                                                         HashMap<String, Object> params) {
        var firstDecision = decisions.stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.DECISION_NOT_FORMED.name()));

        var reasonedRefusalDto = techConditionEpoReportMapper.toTechConditionReasonedRefusalReportDto(firstDecision);
        var dto = techConditionEpoReportMapper.toDecisionReportDto(reasonedRefusalDto);

        this.fillCommonFields(dto, techCondition);

        var addressData = objectAddressDataFromDecisions(decisions);
        dto.setObjectNameRu(addressData.get("objectNameRu"));
        dto.setObjectNameKk(addressData.get("objectNameKk"));
        dto.setObjectAddressRu(addressData.get("objectAddressRu"));
        dto.setObjectAddressKk(addressData.get("objectAddressKk"));

        dto.setSigns(getSignsByIdAndDocType(techCondition.getStatementId(), Collections.singletonList(SignedDocType.REASONED_REFUSAL)));
        dto.setDecisionType(REASONED_REFUSAL);
        dto.setParams(params);

        log.info("REPORT [REASONED REFUSAL]: executionId=[{}], tcId=[{}]", executionId, techCondition.getId());

        return dto;
    }

    private void fillCommonFields(TechConditionEpoApplicationReportDto dto, TechConditionEntity techCondition, UUID id) {
        var providerFullName = providerFullName(techCondition.getProviderId());
        dto.setProviderFullNameRu(providerFullName.get("ru"));
        dto.setProviderFullNameKk(providerFullName.get("kk"));

        var consumerPhoneAndEmail = consumerPhoneAndEmail(techCondition);
        dto.setConsumerPhone(consumerPhoneAndEmail.get("phone"));
        dto.setConsumerEmail(consumerPhoneAndEmail.get("email"));

        var consumerAddress = consumerAddress(techCondition);
        dto.setConsumerAddressRu(consumerAddress.get("ru"));
        dto.setConsumerAddressKk(consumerAddress.get("kk"));
        dto.setFullAddressRu(consumerAddress.get("ru"));
        dto.setFullAddressKk(consumerAddress.get("kk"));

        var objectAddressData = objectAddressData(id);
        dto.setObjectTypeRu(objectAddressData.get("objectNameRu"));
        dto.setObjectTypeKk(objectAddressData.get("objectNameKk"));
        dto.setObjectAddressRu(objectAddressData.get("objectAddressRu"));
        dto.setObjectAddressKk(objectAddressData.get("objectAddressKk"));
        dto.setCadastralNumber(objectAddressData.get("cadastralNumber"));
    }

    private void fillCommonFields(TechConditionEpoDecisionReportDto dto, TechConditionEntity techCondition) {
        var providerFullName = providerFullName(techCondition.getProviderId());
        dto.setProviderFullNameRu(providerFullName.get("ru"));
        dto.setProviderFullNameKk(providerFullName.get("kk"));

        var consumerPhoneAndEmail = consumerPhoneAndEmail(techCondition);
        dto.setConsumerPhone(consumerPhoneAndEmail.get("phone"));
        dto.setConsumerEmail(consumerPhoneAndEmail.get("email"));

        var consumerAddress = consumerAddress(techCondition);
        dto.setConsumerAddressRu(consumerAddress.get("ru"));
        dto.setConsumerAddressKk(consumerAddress.get("kk"));
    }

    private void fillDictionaryFields(TechConditionEpoDecisionReportDto dto, TechConditionEntity techCondition) {
        Optional.ofNullable(dictionaryApiService.findDictionaryValueByCode(techCondition.getConsumptionTypeCode()))
                .ifPresent(d -> {
                    dto.setConsumptionTypeRu(d.getNameRu());
                    dto.setConsumptionTypeKk(d.getNameKz());
                });
        Optional.ofNullable(dictionaryApiService.findDictionaryValueByCode(techCondition.getServiceTypeCode()))
                .ifPresent(d -> {
                    dto.setServiceTypeRu(d.getNameRu());
                    dto.setServiceTypeKk(d.getNameKz());
                });
        Optional.ofNullable(dictionaryApiService.findDictionaryValueByCode(techCondition.getVoltageLevelCode()))
                .ifPresent(d -> {
                    dto.setVoltageLevelRu(d.getNameRu());
                    dto.setVoltageLevelKk(d.getNameKz());
                });
    }

    private Map<String, String> objectAddressDataFromDecisions(List<TechConditionExecutionAbdAddressDecisionEntity> decisions) {
        Map<String, String> result = new HashMap<>();
        try {
            var addresses = decisions.stream()
                    .map(TechConditionExecutionAbdAddressDecisionEntity::getObjectAbdAddress)
                    .collect(Collectors.toList());

            var joinerNameRu = new StringJoiner(", ");
            var joinerNameKk = new StringJoiner(", ");
            var joinerAddressRu = new StringJoiner(", ");
            var joinerAddressKk = new StringJoiner(", ");

            addresses.forEach(a -> {
                joinerNameRu.add(StringUtils.defaultString(a.getEndUseRu()));
                joinerNameKk.add(StringUtils.defaultString(a.getEndUseKk()));
                joinerAddressRu.add(StringUtils.defaultString(a.getLocationRu()));
                joinerAddressKk.add(StringUtils.defaultString(a.getLocationKk()));
            });

            result.put("objectNameRu", joinerNameRu.toString());
            result.put("objectNameKk", joinerNameKk.toString());
            result.put("objectAddressRu", joinerAddressRu.toString());
            result.put("objectAddressKk", joinerAddressKk.toString());

        } catch (Exception e) {
            log.error("Object address from decisions not found: {}", e.getMessage());
            result.put("objectNameRu", null);
            result.put("objectNameKk", null);
            result.put("objectAddressRu", null);
            result.put("objectAddressKk", null);
        }
        return result;
    }

    private Map<String, String> providerFullName(UUID providerId) {
        Map<String, String> result = new HashMap<>();
        try {
            var provider = providerApiService.getProviderDto(providerId);
            switch (provider.getOrganizationType()) {
                case PHYSICAL:
                    result.put("ru", provider.getName());
                    result.put("kk", provider.getName());
                    break;
                case JURIDICAL:
                    result.put("ru", provider.getRu());
                    result.put("kk", provider.getKk());
                    break;
            }
        } catch (Exception e) {
            log.error("Provider not found: {}", e.getMessage());
            result.put("ru", null);
            result.put("kk", null);
        }
        return result;
    }

    private Map<String, String> consumerPhoneAndEmail(TechConditionEntity techCondition) {
        Map<String, String> result = new HashMap<>();
        try {
            var provider = providerApiService.getProviderDto(techCondition.getProviderId());
            var consumer = consumerApiService.getConsumer(
                    techCondition.getConsumerIinBin(),
                    provider.getIinBin(),
                    kz.kus.commons.enums.ConsumerType.valueOf(techCondition.getConsumerType().name()));
            result.put("phone", consumer.getPhone());
            result.put("email", consumer.getEmail());

        } catch (Exception e) {
            log.error("Consumer not found: {}", e.getMessage());
            result.put("phone", null);
            result.put("email", null);
        }
        return result;
    }

    private Map<String, String> consumerAddress(TechConditionEntity techCondition) {
        Map<String, String> result = new HashMap<>();
        try {
            String arRcaCode;
            if (ORGANIZATION.equals(techCondition.getConsumerType())) {
                var organization = organizationApi.getOrganization(techCondition.getConsumerIinBin());
                arRcaCode = organization.getAddressInfo().getRca();
            } else {
                var person = personApi.getPerson(techCondition.getConsumerIinBin());
                arRcaCode = person.getRegistrationAddress().getAddressArCode();
            }
            var address = arApiService.fullAddress(arRcaCode);
            result.put("ru", address.getShortAddressRu());
            result.put("kk", address.getShortAddressKk());
        } catch (Exception e) {
            log.error("Consumer address not found: {}", e.getMessage());
            result.put("ru", null);
            result.put("kk", null);
        }
        return result;
    }

    private Map<String, String> objectAddressData(UUID techConditionExecutionId) {
        Map<String, String> result = new HashMap<>();
        try {
            var list = abdAddressService.getByTechConditionExecutionId(techConditionExecutionId);
            if (!list.isEmpty()) {
                StringJoiner joinerNameRu = new StringJoiner(",");
                StringJoiner joinerNameKk = new StringJoiner(",");
                StringJoiner joinerAddressRu = new StringJoiner(",");
                StringJoiner joinerAddressKk = new StringJoiner(",");
                StringJoiner joinerDocumentRu = new StringJoiner(",");
                StringJoiner joinerDocumentKk = new StringJoiner(",");
                StringJoiner joinerStoreys = new StringJoiner(",");
                StringJoiner joinerTotalArea = new StringJoiner(",");
                StringJoiner joinerCadastralNumber = new StringJoiner(",");
                StringJoiner joinerArRcaCode = new StringJoiner(",");
                list.forEach(item -> {
                    joinerNameRu.add(item.getEndUseRu());
                    joinerNameKk.add(item.getEndUseKk());
                    joinerAddressRu.add(item.getLocationRu());
                    joinerAddressKk.add(item.getLocationKk());
                    joinerDocumentRu.add(item.getDocumentRu());
                    joinerDocumentKk.add(item.getDocumentKk());
                    joinerStoreys.add(item.getStoreys());
                    joinerTotalArea.add(item.getTotalArea());
                    joinerCadastralNumber.add(item.getCadastralNumber());
                    joinerArRcaCode.add(item.getArRcaCode());
                });
                result.put("objectNameRu", joinerNameRu.toString());
                result.put("objectNameKk", joinerNameKk.toString());
                result.put("objectAddressRu", joinerAddressRu.toString());
                result.put("objectAddressKk", joinerAddressKk.toString());
                result.put("ownershipDocumentTypeRu", joinerDocumentRu.toString());
                result.put("ownershipDocumentTypeKk", joinerDocumentKk.toString());
                result.put("storeys", joinerStoreys.toString());
                result.put("totalArea", joinerTotalArea.toString());
                result.put("cadastralNumber", joinerCadastralNumber.toString());
                result.put("arRcaCode", joinerArRcaCode.toString());
            }
        } catch (Exception e) {
            log.error("Object address not found: {}", e.getMessage());
            result.put("objectNameRu", null);
            result.put("objectNameKk", null);
            result.put("objectAddressRu", null);
            result.put("objectAddressKk", null);
            result.put("ownershipDocumentTypeRu", null);
            result.put("ownershipDocumentTypeKk", null);
            result.put("storeys", null);
            result.put("totalArea", null);
            result.put("cadastralNumber", null);
            result.put("arRcaCode", null);
        }
        return result;
    }

    private void setSubConsumers(UUID techConditionId, HashMap<String, Object> params) {
        var list = techConditionSubConsumerService.getByTechConditionId(techConditionId);
        if (!list.isEmpty()) {
            StringJoiner joinerRu = new StringJoiner(",\n");
            StringJoiner joinerKk = new StringJoiner(",\n");
            for (TechConditionSubConsumerEntity item : list) {
                if (nonNull(item.getFullName())) {
                    joinerRu.add("ФИО субпотребителя: " + item.getFullName());
                    joinerKk.add("Қосалқы тұтынушының ТАӘ: " + item.getFullName());
                }
                joinerRu.add("Потребляемая мощность: " + StringUtils.defaultString(item.getPowerConsumption()));
                joinerKk.add("Тұтыну қуаты: " + StringUtils.defaultString(item.getPowerConsumption()));
            }
            params.put("subConsumersRu", joinerRu.toString());
            params.put("subConsumersKk", joinerKk.toString());
        }
    }

    private void setReliabilityCategoriesWithKwt(UUID techConditionId, HashMap<String, Object> params) {
        var list = techConditionReliabilityCategoryService.getByTechConditionId(techConditionId);
        if (!list.isEmpty()) {
            StringJoiner joinerRu = new StringJoiner(",\n");
            StringJoiner joinerKk = new StringJoiner(",\n");
            for (TechConditionReliabilityCategoryEntity item : list) {
                StringBuilder sbRu = new StringBuilder();
                StringBuilder sbKk = new StringBuilder();
                Optional.ofNullable(dictionaryApiService.findDictionaryValueByCode(item.getReliabilityCategoryTypeCode()))
                        .ifPresent(value -> {
                            sbRu.append(value.getNameRu());
                            sbKk.append(value.getNameKz());
                        });
                if (nonNull(item.getKwt())) {
                    sbRu.append(" - ").append(item.getKwt()).append(" кВт (кВА)");
                    sbKk.append(" - ").append(item.getKwt()).append(" кВт (кВА)");
                }
                joinerRu.add(sbRu.toString());
                joinerKk.add(sbKk.toString());
            }
            params.put("reliabilityCategoriesRu", joinerRu.toString());
            params.put("reliabilityCategoriesKk", joinerKk.toString());
        }
    }

    private List<SignReportDto> getSignsByIdAndDocType(UUID id, List<SignedDocType> docTypes) {
        List<StatementSignDto> result = new ArrayList<>();
        result.addAll(registrySignApiService.getConsumerSign(id, docTypes));
        result.addAll(registrySignApiService.getProviderSign(id, docTypes));
        return techConditionEpoReportMapper.toReportDtoList(result);
    }

    private TechConditionExecutionEntity findExecutionById(UUID id) {
        return techConditionExecutionRepository.findByIdAndDeletedDatetimeIsNull(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }
}
