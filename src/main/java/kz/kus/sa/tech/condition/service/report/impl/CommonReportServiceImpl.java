package kz.kus.sa.tech.condition.service.report.impl;

import kz.kus.commons.enums.ConsumerType;
import kz.kus.sa.auth.api.provider.ProviderApiService;
import kz.kus.sa.auth.api.provider.dto.ProviderDto;
import kz.kus.sa.consumer.api.ConsumerApiService;
import kz.kus.sa.consumer.dto.consumer.view.IndividualEntrepreneurViewConsumerDto;
import kz.kus.sa.file.api.FileApiService;
import kz.kus.sa.file.dto.ImageFormatDto;
import kz.kus.sa.fl.api.PersonApi;
import kz.kus.sa.registry.dto.common.FileCreateDto;
import kz.kus.sa.tech.condition.service.report.CommonReportService;
import kz.kus.sa.ul.api.OrganizationApi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.nonNull;
import static kz.kus.sa.tech.condition.util.CommonUtils.getFullName;

@Slf4j
@Service
@AllArgsConstructor
public class CommonReportServiceImpl implements CommonReportService {

    private final PersonApi personApi;
    private final FileApiService fileApiService;
    private final OrganizationApi organizationApi;
    private final ProviderApiService providerApiService;
    private final ConsumerApiService consumerApiService;

    @Override
    public void setBlankHeaderInfo(UUID providerId, HashMap<String, Object> params) {
        ProviderDto provider = providerApiService.getProviderDto(providerId);
        if (nonNull(provider)) {
            params.put("licenseNumber", provider.getLicenseNumber());
            params.put("licenseDate", provider.getLicenseDate());
            params.put("nameRu", provider.getRu());
            params.put("nameKz", provider.getKk());
            params.put("addressRu", provider.getLegalAddressRu());
            params.put("addressKz", provider.getLegalAddressKk());
            params.put("contact", provider.getContact());
            params.put("email", provider.getEmail());
            params.put("fioHead", provider.getFioHead());
            params.put("fioHeadDativeCase", provider.getFioHeadDativeCase());
            params.put("positionHeadRu", provider.getPositionHeadRu());
            params.put("positionHeadKz", provider.getPositionHeadKk());
            params.put("positionHeadDativeCaseRu", provider.getPositionHeadDativeCaseRu());
            params.put("positionHeadDativeCaseKz", provider.getPositionHeadDativeCaseKk());
            params.put("bin", provider.getIinBin());
            params.put("iik", provider.getIik());
            params.put("bik", provider.getBik());
            params.put("bankName", provider.getBankName());
            params.put("cityKz", provider.getCityKk());
            params.put("cityRu", provider.getCityRu());
            params.put("templateHeaderRu", provider.getTemplateHeaderRu());
            params.put("templateHeaderKk", provider.getTemplateHeaderKk());

            if (nonNull(provider.getFileCreateDto())) {
                UUID logoExternalId = provider.getFileCreateDto().getId();
                if (isValidFormat(logoExternalId)) {
                    byte[] imageBytes = fileApiService.download(logoExternalId);
                    params.put("logo", Base64.encodeBase64String(imageBytes));
                }
            }
        }
    }

    @Override
    public void setSystemOperatorRequirementFile(FileCreateDto file, HashMap<String, Object> params) {
        if (nonNull(file)) {
            UUID externalId = file.getId();
            if (isValidFormat(externalId)) {
                byte[] imageBytes = fileApiService.download(externalId);
                params.put("SOR", Base64.encodeBase64String(imageBytes));
            }
        }
    }

    @Override
    public void setBorderDemarcationSchemeFile(FileCreateDto file, HashMap<String, Object> params) {
        if (nonNull(file)) {
            UUID externalId = file.getId();
            if (isValidFormat(externalId)) {
                byte[] imageBytes = fileApiService.download(externalId);
                params.put("scheme", Base64.encodeBase64String(imageBytes));
            }
        }
    }

    private boolean isValidFormat(UUID externalId) {
        ImageFormatDto imageFormat = fileApiService.getImageFormat(externalId);
        return StringUtils.isNotBlank(imageFormat.getFormat())
                && (List.of("PNG", "JPEG", "TIF").contains(imageFormat.getFormat()));
    }

    @Override
    public Map<String, String> getConsumerFullName(ConsumerType consumerType, String consumerIinBin, String providerBin) {
        Map<String, String> result = new HashMap<>();

        try {
            if (ConsumerType.PERSON.equals(consumerType)) {
                var person = personApi.getPerson(consumerIinBin);
                result.put("ru", getFullName(person.getLastName(), person.getFirstName(), person.getMiddleName()));
                result.put("kk", getFullName(person.getLastName(), person.getFirstName(), person.getMiddleName()));
            } else if (ConsumerType.ORGANIZATION.equals(consumerType)) {
                var organization = organizationApi.getOrganization(consumerIinBin);
                result.put("ru", organization.getMainInfo().getFullNameRu());
                result.put("kk", organization.getMainInfo().getFullNameKk());
            } else if (ConsumerType.INDIVIDUAL_ENTREPRENEUR.equals(consumerType)) {
                var consumer = (IndividualEntrepreneurViewConsumerDto) consumerApiService.getConsumer(
                        consumerIinBin,
                        providerBin,
                        consumerType);
                result.put("ru", consumer.getNameRu());
                result.put("kk", consumer.getNameKk());
            }
        } catch (Exception e) {
            log.error("ERROR consumerType = {}, consumerIinBin = {}, providerBin = {} not found",
                    consumerType, consumerIinBin, providerBin);
            result.put("ru", null);
            result.put("kk", null);
        }

        return result;
    }
}
