package kz.kus.sa.tech.condition.service.report.impl;

import kz.kus.sa.ar.api.ArApiService;
import kz.kus.sa.auth.api.provider.ProviderApiService;
import kz.kus.sa.consumer.api.ConsumerApiService;
import kz.kus.sa.fl.api.PersonApi;
import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionEntity;
import kz.kus.sa.tech.condition.dao.mapper.TechConditionEpoReportMapper;
import kz.kus.sa.tech.condition.dao.repository.TechConditionExecutionRepository;
import kz.kus.sa.tech.condition.dto.report.epo.TechConditionEpoApplicationReportDto;
import kz.kus.sa.tech.condition.exception.ErrorCode;
import kz.kus.sa.tech.condition.exception.NotFoundException;
import kz.kus.sa.tech.condition.service.address.AbdAddressService;
import kz.kus.sa.tech.condition.service.report.CommonReportService;
import kz.kus.sa.tech.condition.service.report.TechConditionEpoReportService;
import kz.kus.sa.ul.api.OrganizationApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

import static kz.kus.commons.enums.ConsumerType.ORGANIZATION;

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
    private final TechConditionEpoReportMapper techConditionEpoReportMapper;
    private final TechConditionExecutionRepository techConditionExecutionRepository;

    @Override
    public TechConditionEpoApplicationReportDto applicationReportData(UUID id) {
        var entity = findExecutionById(id);
        var techConditionEntity = entity.getTechCondition();

        var dto = techConditionEpoReportMapper.toApplicationReportDto(entity);

        this.fillCommonFields(dto, techConditionEntity, id);

        HashMap<String, Object> params = new HashMap<>();
        commonReportService.setBlankHeaderInfo(techConditionEntity.getProviderId(), params);

        dto.setParams(params);
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

    private TechConditionExecutionEntity findExecutionById(UUID id) {
        return techConditionExecutionRepository.findByIdAndDeletedDatetimeIsNull(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }
}
