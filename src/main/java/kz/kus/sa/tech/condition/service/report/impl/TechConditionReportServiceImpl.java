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
import kz.kus.sa.tech.condition.dao.mapper.TechConditionReportMapper;
import kz.kus.sa.tech.condition.dao.repository.TechConditionExecutionRepository;
import kz.kus.sa.tech.condition.dto.report.TechConditionApplicationReportDto;
import kz.kus.sa.tech.condition.dto.report.TechConditionDecisionReportDto;
import kz.kus.sa.tech.condition.dto.report.TechConditionReasonedRefusalReportDto;
import kz.kus.sa.tech.condition.dto.report.TechConditionTechRecommendationReportDto;
import kz.kus.sa.tech.condition.exception.BadRequestException;
import kz.kus.sa.tech.condition.exception.BusinessException;
import kz.kus.sa.tech.condition.exception.ErrorCode;
import kz.kus.sa.tech.condition.exception.NotFoundException;
import kz.kus.sa.tech.condition.service.address.AbdAddressService;
import kz.kus.sa.tech.condition.service.report.CommonReportService;
import kz.kus.sa.tech.condition.service.report.TechConditionReportService;
import kz.kus.sa.tech.condition.service.tech.condition.*;
import kz.kus.sa.ul.api.OrganizationApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static kz.kus.commons.enums.ConsumerType.ORGANIZATION;
import static kz.kus.sa.registry.enums.TechConditionExecutionDecisionType.REASONED_REFUSAL;
import static kz.kus.sa.registry.enums.TechConditionExecutionDecisionType.TECHNICAL_RECOMMENDATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class TechConditionReportServiceImpl implements TechConditionReportService {

    private final PersonApi personApi;
    private final ArApiService arApiService;
    private final OrganizationApi organizationApi;
    private final AbdAddressService abdAddressService;
    private final ConsumerApiService consumerApiService;
    private final ProviderApiService providerApiService;
    private final CommonReportService commonReportService;
    private final DictionaryApiService dictionaryApiService;
    private final RegistrySignApiService registrySignApiService;
    private final TechConditionReportMapper techConditionReportMapper;
    private final TechConditionSubConsumerService techConditionSubConsumerService;
    private final TechConditionMaximumLoadService techConditionMaximumLoadService;
    private final TechConditionExecutionRepository techConditionExecutionRepository;
    private final TechConditionPlannedEquipmentService techConditionPlannedEquipmentService;
    private final TechConditionReliabilityCategoryService techConditionReliabilityCategoryService;
    private final TechConditionContractualCapacityOfTransformerService techConditionContractualCapacityOfTransformerService;

    @Override
    public TechConditionApplicationReportDto applicationReportData(UUID id) {
        TechConditionExecutionEntity entity = findExecutionById(id);
        TechConditionEntity techConditionEntity = entity.getTechCondition();

        TechConditionApplicationReportDto dto = techConditionReportMapper.toApplicationReportDto(entity);

        var providerFullName = providerFullName(techConditionEntity.getProviderId());
        dto.setProviderFullNameRu(providerFullName.get("ru"));
        dto.setProviderFullNameKk(providerFullName.get("kk"));

        var consumerPhoneAndEmail = consumerPhoneAndEmail(techConditionEntity);
        dto.setConsumerPhone(consumerPhoneAndEmail.get("phone"));
        dto.setPhones(consumerPhoneAndEmail.get("phone"));
        dto.setConsumerEmail(consumerPhoneAndEmail.get("email"));

        var consumerAddress = consumerAddress(techConditionEntity);
        dto.setConsumerAddressRu(consumerAddress.get("ru"));
        dto.setConsumerAddressKk(consumerAddress.get("kk"));
        dto.setFullAddressRu(consumerAddress.get("ru"));
        dto.setFullAddressKk(consumerAddress.get("kk"));

        var objectAddressData = objectAddressData(id);
        dto.setObjectTypeRu(objectAddressData.get("objectNameRu"));
        dto.setObjectTypeKk(objectAddressData.get("objectNameKk"));
        dto.setObjectAddressRu(objectAddressData.get("objectAddressRu"));
        dto.setObjectAddressKk(objectAddressData.get("objectAddressKk"));
        dto.setOwnershipDocumentTypeRu(objectAddressData.get("ownershipDocumentTypeRu"));
        dto.setOwnershipDocumentTypeKk(objectAddressData.get("ownershipDocumentTypeKk"));
        dto.setCadastralNumber(objectAddressData.get("cadastralNumber"));
        dto.setNumberOfStoreys(objectAddressData.get("storeys"));
        dto.setBuildingArea(objectAddressData.get("totalArea"));

        HashMap<String, Object> params = new HashMap<>();
        commonReportService.setBlankHeaderInfo(techConditionEntity.getProviderId(), params);
        setMaximumLoads(id, params);
        setMaximumLoadsWithElectricalReceiversLoad(id, params);
        setPlannedEquipment(id, params);
        setSubConsumers(id, params);
        setContractualCapacityOfTransformer(id, params);
        setReliabilityCategoriesWithKwt(id, params);

        Optional.ofNullable(dictionaryApiService.findDictionaryValueByCode(techConditionEntity.getConsumptionTypeCode()))
                .ifPresent(d -> {
                    dto.setConsumptionTypeRu(d.getNameRu());
                    dto.setConsumptionTypeKk(d.getNameKz());
                });
        Optional.ofNullable(dictionaryApiService.findDictionaryValueByCode(techConditionEntity.getElectricalLoadTypeCode()))
                .ifPresent(d -> {
                    dto.setElectricalLoadTypeRu(d.getNameRu());
                    dto.setElectricalLoadTypeKk(d.getNameKz());
                });
        Optional.ofNullable(dictionaryApiService.findDictionaryValueByCode(techConditionEntity.getVoltageLevelCode()))
                .ifPresent(d -> {
                    dto.setVoltageLevelRu(d.getNameRu());
                    dto.setVoltageLevelKk(d.getNameKz());
                });
        Optional.ofNullable(dictionaryApiService.findDictionaryValueByCode(techConditionEntity.getServiceTypeCode()))
                .ifPresent(d -> {
                    dto.setServiceTypeRu(d.getNameRu());
                    dto.setServiceTypeKk(d.getNameKz());
                });

        dto.setParams(params);
        return dto;
    }

    @Override
    public TechConditionDecisionReportDto getDecisionReportData(UUID id) {
        TechConditionExecutionEntity entity = findExecutionById(id);
        TechConditionEntity techConditionEntity = entity.getTechCondition();

        if (isNull(entity.getDecisionType())) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
        }

        HashMap<String, Object> params = new HashMap<>();
        commonReportService.setBlankHeaderInfo(techConditionEntity.getProviderId(), params);

        if (TECHNICAL_RECOMMENDATION == entity.getDecisionType()) {
            if (isNull(entity.getProject())) {
                throw new BusinessException(ErrorCode.DECISION_NOT_FORMED.name());
            }
            TechConditionTechRecommendationReportDto techRecommendationDto = techConditionReportMapper.toTechnicalRecommendationReportDto(entity);
            TechConditionDecisionReportDto dto = techConditionReportMapper.toDecisionReportDto(techRecommendationDto);

            var providerFullName = providerFullName(techConditionEntity.getProviderId());
            dto.setProviderFullNameRu(providerFullName.get("ru"));
            dto.setProviderFullNameKk(providerFullName.get("kk"));

            var consumerPhoneAndEmail = consumerPhoneAndEmail(techConditionEntity);
            dto.setConsumerPhone(consumerPhoneAndEmail.get("phone"));
            dto.setConsumerEmail(consumerPhoneAndEmail.get("email"));

            var consumerAddress = consumerAddress(techConditionEntity);
            dto.setConsumerAddressRu(consumerAddress.get("ru"));
            dto.setConsumerAddressKk(consumerAddress.get("kk"));

            var objectAddressData = objectAddressData(id);
            dto.setObjectNameRu(objectAddressData.get("objectNameRu"));
            dto.setObjectNameKk(objectAddressData.get("objectNameKk"));
            dto.setObjectAddressRu(objectAddressData.get("objectAddressRu"));
            dto.setObjectAddressKk(objectAddressData.get("objectAddressKk"));

            setSubConsumers(id, params);
            setReliabilityCategoriesWithKwt(id, params);

            Optional.ofNullable(dictionaryApiService.findDictionaryValueByCode(techConditionEntity.getTechConditionReasonCode()))
                    .ifPresent(d -> {
                        dto.setReasonRu(d.getNameRu());
                        dto.setReasonKk(d.getNameKz());
                    });
            Optional.ofNullable(dictionaryApiService.findDictionaryValueByCode(techConditionEntity.getConsumptionTypeCode()))
                    .ifPresent(d -> {
                        dto.setConsumptionTypeRu(d.getNameRu());
                        dto.setConsumptionTypeKk(d.getNameKz());
                    });
            Optional.ofNullable(dictionaryApiService.findDictionaryValueByCode(techConditionEntity.getElectricalLoadTypeCode()))
                    .ifPresent(d -> {
                        dto.setElectricalLoadTypeRu(d.getNameRu());
                        dto.setElectricalLoadTypeKk(d.getNameKz());
                    });
            Optional.ofNullable(dictionaryApiService.findDictionaryValueByCode(techConditionEntity.getServiceTypeCode()))
                    .ifPresent(d -> {
                        dto.setServiceTypeRu(d.getNameRu());
                        dto.setServiceTypeKk(d.getNameKz());
                    });
            Optional.ofNullable(dictionaryApiService.findDictionaryValueByCode(techConditionEntity.getVoltageLevelCode()))
                    .ifPresent(d -> {
                        dto.setVoltageLevelRu(d.getNameRu());
                        dto.setVoltageLevelKk(d.getNameKz());
                    });

            commonReportService.setSystemOperatorRequirementFile(techRecommendationDto.getSystemOperatorRequirementFile(), params);

            dto.setSigns(getSignsByIdAndDocType(techConditionEntity.getStatementId(), Collections.singletonList(SignedDocType.TC_PROJECT)));
            dto.setDecisionType(entity.getDecisionType());
            dto.setParams(params);

            log.info("REPORT [TECHNICAL RECOMMENDATION]: execution id = [{}], id = [{}]", id, techConditionEntity.getId());
            return dto;
        } else if (REASONED_REFUSAL == entity.getDecisionType()) {
            TechConditionReasonedRefusalReportDto reasonedRefusalDto = techConditionReportMapper.toTechConditionReasonedRefusalReportDto(entity);
            TechConditionDecisionReportDto dto = techConditionReportMapper.toDecisionReportDto(reasonedRefusalDto);

            var providerFullName = providerFullName(techConditionEntity.getProviderId());
            dto.setProviderFullNameRu(providerFullName.get("ru"));
            dto.setProviderFullNameKk(providerFullName.get("kk"));

            var consumerPhoneAndEmail = consumerPhoneAndEmail(techConditionEntity);
            dto.setConsumerPhone(consumerPhoneAndEmail.get("phone"));
            dto.setConsumerEmail(consumerPhoneAndEmail.get("email"));

            var consumerAddress = consumerAddress(techConditionEntity);
            dto.setConsumerAddressRu(consumerAddress.get("ru"));
            dto.setConsumerAddressKk(consumerAddress.get("kk"));

            var objectAddressData = objectAddressData(id);
            dto.setObjectNameRu(objectAddressData.get("objectNameRu"));
            dto.setObjectNameKk(objectAddressData.get("objectNameKk"));
            dto.setObjectAddressRu(objectAddressData.get("objectAddressRu"));
            dto.setObjectAddressKk(objectAddressData.get("objectAddressKk"));

            dto.setSigns(getSignsByIdAndDocType(techConditionEntity.getStatementId(), Collections.singletonList(SignedDocType.REASONED_REFUSAL)));
            dto.setDecisionType(entity.getDecisionType());
            dto.setParams(params);

            log.info("REPORT [REASONED REFUSAL]: execution id = [{}], id = [{}]", id, techConditionEntity.getId());
            return dto;
        }
        throw new BadRequestException(ErrorCode.BAD_REQUEST.name());
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
                var person = personApi.getPerson(techCondition.getConsumerIinBin());
                arRcaCode = person.getRegistrationAddress().getAddressArCode();
            } else {
                var organization = organizationApi.getOrganization(techCondition.getConsumerIinBin());
                arRcaCode = organization.getAddressInfo().getRca();
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
            List<AbdAddressEntity> list = abdAddressService.getByTechConditionExecutionId(techConditionExecutionId);
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

    private void setMaximumLoads(UUID techConditionId, HashMap<String, Object> params) {
        List<TechConditionMaximumLoadEntity> list = techConditionMaximumLoadService.getByTechConditionId(techConditionId);
        if (!list.isEmpty()) {
            StringJoiner joinerRu = new StringJoiner(",\n");
            StringJoiner joinerKk = new StringJoiner(",\n");
            for (TechConditionMaximumLoadEntity item : list) {
                if (nonNull(item.getElectricalReceiversLoadKw())) {
                    joinerRu.add(StringUtils.defaultString(item.getPeriod()) + " г. - " + StringUtils.defaultString(item.getLoad().toString()) + " кВт");
                    joinerKk.add(StringUtils.defaultString(item.getPeriod()) + " ж. - " + StringUtils.defaultString(item.getLoad().toString()) + " кВт");
                }
            }
            params.put("maximumLoads1Ru", joinerRu.toString());
            params.put("maximumLoads1Kk", joinerKk.toString());
        }
    }

    private void setMaximumLoadsWithElectricalReceiversLoad(UUID techConditionId, HashMap<String, Object> params) {
        List<TechConditionMaximumLoadEntity> list = techConditionMaximumLoadService.getByTechConditionId(techConditionId);
        if (!list.isEmpty()) {
            StringJoiner joinerRu = new StringJoiner(",\n");
            StringJoiner joinerKk = new StringJoiner(",\n");
            for (TechConditionMaximumLoadEntity item : list) {
                if (nonNull(item.getReliabilityCategoryTypeCode())) {
                    Optional.ofNullable(dictionaryApiService.findDictionaryValueByCode(item.getReliabilityCategoryTypeCode()))
                            .ifPresent(d -> {
                                joinerRu.add(d.getNameRu() + " " + StringUtils.defaultString(item.getElectricalReceiversLoadKw().toString()) + " кВт (кВА)");
                                joinerKk.add(d.getNameKz() + " " + StringUtils.defaultString(item.getElectricalReceiversLoadKw().toString()) + " кВт (кВА)");
                            });
                }
            }
            params.put("maximumLoads2Ru", joinerRu.toString());
            params.put("maximumLoads2Kk", joinerKk.toString());
        }
    }

    private void setPlannedEquipment(UUID techConditionId, HashMap<String, Object> params) {
        List<TechConditionPlannedEquipmentEntity> list = techConditionPlannedEquipmentService.getByTechConditionId(techConditionId);
        if (!list.isEmpty()) {
            StringJoiner joinerRu = new StringJoiner(",\n");
            StringJoiner joinerKk = new StringJoiner(",\n");
            for (TechConditionPlannedEquipmentEntity item : list)
                if (nonNull(item.getEquipmentTypeCode())) {
                    Optional.ofNullable(dictionaryApiService.findDictionaryValueByCode(item.getEquipmentTypeCode()))
                            .ifPresent(d -> {
                                joinerRu.add(d.getNameRu() + " в количестве " + StringUtils.defaultString(item.getCount().toString()) + " штук, единичной мощности " + StringUtils.defaultString(item.getUnitPower().toString()) + " кВт (кВА)");
                                joinerKk.add(d.getNameKz() + " " + StringUtils.defaultString(item.getCount().toString()) + " дана, бірлік қуаты " + StringUtils.defaultString(item.getUnitPower().toString()) + " кВт (кВА)");
                            });
                }
            params.put("plannedEquipmentRu", joinerRu.toString());
            params.put("plannedEquipmentKk", joinerKk.toString());
        }
    }

    private void setSubConsumers(UUID techConditionId, HashMap<String, Object> params) {
        List<TechConditionSubConsumerEntity> list = techConditionSubConsumerService.getByTechConditionId(techConditionId);
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

    private void setContractualCapacityOfTransformer(UUID techConditionId, HashMap<String, Object> params) {
        List<TechConditionContractualCapacityOfTransformerEntity> list = techConditionContractualCapacityOfTransformerService.getByTechConditionId(techConditionId);
        StringJoiner joiner = new StringJoiner(",\n");
        if (!list.isEmpty()) {
            for (TechConditionContractualCapacityOfTransformerEntity item : list) {
                joiner.add("№ " + StringUtils.defaultString(item.getTransformerNumber()) + " - " + StringUtils.defaultString(item.getKwt().toString()) + " кВт (кВА)");
            }
        }
        params.put("contractualCapacityOfTransformers", joiner.toString());
    }

    private void setReliabilityCategoriesWithKwt(UUID techConditionId, HashMap<String, Object> params) {
        List<TechConditionReliabilityCategoryEntity> list = techConditionReliabilityCategoryService.getByTechConditionId(techConditionId);
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
        return techConditionReportMapper.toReportDtoList(result);
    }

    private TechConditionExecutionEntity findExecutionById(UUID id) {
        return techConditionExecutionRepository.findByIdAndDeletedDatetimeIsNull(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }
}
