package kz.kus.sa.tech.condition.service.report.impl;

import kz.kus.commons.enums.ConsumerType;
import kz.kus.sa.auth.api.provider.ProviderApiService;
import kz.kus.sa.consumer.api.ConsumerApiService;
import kz.kus.sa.dictionary.api.DictionaryApiService;
import kz.kus.sa.registry.api.RegistrySignApiService;
import kz.kus.sa.registry.dto.report.v1.SignReportDto;
import kz.kus.sa.registry.dto.v1.StatementSignDto;
import kz.kus.sa.registry.enums.SignedDocType;
import kz.kus.sa.tech.condition.dao.entity.AbdAddressEntity;
import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationEntity;
import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationRenewalEntity;
import kz.kus.sa.tech.condition.dao.entity.ElectrifiedInstallationEntity;
import kz.kus.sa.tech.condition.dao.mapper.ActOfDelineationReportMapper;
import kz.kus.sa.tech.condition.dao.repository.ActOfDelineationRenewalRepository;
import kz.kus.sa.tech.condition.dto.report.act.AbdAddressReportDto;
import kz.kus.sa.tech.condition.dto.report.act.ActOfDelineationRenewalApplicationReportDto;
import kz.kus.sa.tech.condition.dto.report.act.ActOfDelineationRenewalReportDto;
import kz.kus.sa.tech.condition.dto.report.act.ElectrifiedInstallationReportDto;
import kz.kus.sa.tech.condition.exception.ErrorCode;
import kz.kus.sa.tech.condition.exception.NotFoundException;
import kz.kus.sa.tech.condition.service.act.ActOfDelineationService;
import kz.kus.sa.tech.condition.service.act.ElectrifiedInstallationService;
import kz.kus.sa.tech.condition.service.address.AbdAddressService;
import kz.kus.sa.tech.condition.service.report.ActOfDelineationReportService;
import kz.kus.sa.tech.condition.service.report.CommonReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActOfDelineationReportServiceImpl implements ActOfDelineationReportService {

    private final AbdAddressService abdAddressService;
    private final ProviderApiService providerApiService;
    private final ConsumerApiService consumerApiService;
    private final CommonReportService commonReportService;
    private final DictionaryApiService dictionaryApiService;
    private final RegistrySignApiService registrySignApiService;
    private final ActOfDelineationService actOfDelineationService;
    private final ActOfDelineationReportMapper actOfDelineationReportMapper;
    private final ElectrifiedInstallationService electrifiedInstallationService;
    private final ActOfDelineationRenewalRepository actOfDelineationRenewalRepository;

    @Override
    public ActOfDelineationRenewalApplicationReportDto applicationReportData(UUID id) {
        var renewal = findById(id);

        var dto = actOfDelineationReportMapper.toActOfDelineationRenewalApplicationReportDto(renewal);

        setConsumer(dto, renewal);

        var objectAddressData = objectAddressData(renewal);
        dto.setObjectAddressRu(objectAddressData.get("objectAddressRu"));
        dto.setObjectAddressKk(objectAddressData.get("objectAddressKk"));

        HashMap<String, Object> params = new HashMap<>();
        commonReportService.setBlankHeaderInfo(renewal.getProviderId(), params);
        dto.setParams(params);

        log.info("REPORT [APPLICATION BLANK]: id = [{}]", id);
        return dto;
    }

    @Override
    public ActOfDelineationRenewalReportDto actReportData(UUID id) {
        var renewal = findById(id);
        var act = actOfDelineationService.findByRenewalExecutionId(id);

        var dto = actOfDelineationReportMapper.toActOfDelineationRenewalReportDto(act);

        HashMap<String, Object> params = new HashMap<>();
        commonReportService.setBlankHeaderInfo(act.getProviderId(), params);

        dto.setObjectAbdAddresses(getObjectAbdAddresses(act, params));
        dto.setElectrifiedInstallations(getElectrifiedInstallations(act));
        dto.setSigns(getSignsByIdAndDocType(renewal.getStatementId(), Collections.singletonList(SignedDocType.ACT_OF_DELINEATION)));
        dto.setParams(params);

        log.info("REPORT [ACT BLANK]: id = [{}]", id);
        return dto;
    }

    private ActOfDelineationRenewalEntity findById(UUID id) {
        return actOfDelineationRenewalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    private void setConsumer(ActOfDelineationRenewalApplicationReportDto dto, ActOfDelineationRenewalEntity entity) {
        var providerDto =  providerApiService.getProviderDto(entity.getProviderId());
        var consumer = consumerApiService.getConsumer(
                entity.getConsumerIinBin(),
                providerDto.getIinBin(),
                ConsumerType.valueOf(entity.getConsumerType().name()));

        Map<String, String> consumerName = commonReportService.getConsumerFullName(
                consumer.getType(),
                consumer.getIdentifier(),
                consumer.getProviderBin());
        dto.setConsumerFullNameRu(consumerName.get("ru"));
        dto.setConsumerFullNameKk(consumerName.get("kk"));
        dto.setConsumerPhone(consumer.getPhone());
        dto.setConsumerEmail(consumer.getEmail());

        if (nonNull(entity.getRepresentativePerson()) && nonNull(dto.getRepresentativeFullName()))
            dto.setRepresentativeEmail(consumer.getEmail());
    }

    private Map<String, String> objectAddressData(ActOfDelineationRenewalEntity entity) {
        Map<String, String> result = new HashMap<>();
        try {
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
            entity.getObjectAbdAddresses().forEach(item -> {
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

    private  List<AbdAddressReportDto> getObjectAbdAddresses(ActOfDelineationEntity act, HashMap<String, Object> params) {
        List<AbdAddressReportDto> result = new ArrayList<>();
        List<AbdAddressEntity> addressList = abdAddressService.getByActOfDelineationId(act.getId());
        for (int i = 0; i < act.getObjectAbdAddresses().size(); i++) {
            AbdAddressEntity address = addressList.get(i);

            AbdAddressReportDto dto = actOfDelineationReportMapper.toAbdAddressReport(address);
            dto.setNumber(i + 1);
            Optional.ofNullable(dictionaryApiService.findDictionaryValueByCode(address.getVoltageLevelCode()))
                    .ifPresent(value -> {
                        dto.setVoltageLevelRu(value.getNameRu());
                        dto.setVoltageLevelKk(value.getNameKz());
                    });
            commonReportService.setBorderDemarcationSchemeFile(dto.getBorderDemarcationSchemeFile(), params);
            dto.setParams(params);

            result.add(dto);
        }
        return result;
    }

    private  List<ElectrifiedInstallationReportDto> getElectrifiedInstallations(ActOfDelineationEntity act) {
        List<ElectrifiedInstallationReportDto> result = new ArrayList<>();
        List<ElectrifiedInstallationEntity> installationList = electrifiedInstallationService.getByActOfDelineationId(act.getId());
        for (int i = 0; i < act.getElectrifiedInstallations().size(); i++) {
            ElectrifiedInstallationEntity installation = installationList.get(i);

            ElectrifiedInstallationReportDto dto = actOfDelineationReportMapper.toElectrifiedInstallationReport(installation);
            dto.setNumber(i + 1);

            result.add(dto);
        }
        return result;
    }

    private List<SignReportDto> getSignsByIdAndDocType(UUID id, List<SignedDocType> docTypes) {
        List<StatementSignDto> result = new ArrayList<>();
        result.addAll(registrySignApiService.getConsumerSign(id, docTypes));
        result.addAll(registrySignApiService.getProviderSign(id, docTypes));
        return actOfDelineationReportMapper.toReportDtoList(result);
    }
}
