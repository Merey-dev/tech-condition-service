package kz.kus.sa.tech.condition.service.report;

import kz.kus.commons.enums.ConsumerType;
import kz.kus.sa.registry.dto.common.FileCreateDto;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface CommonReportService {

    void setBlankHeaderInfo(UUID providerId, HashMap<String, Object> params);

    void setSystemOperatorRequirementFile(FileCreateDto file, HashMap<String, Object> params);

    void setBorderDemarcationSchemeFile(FileCreateDto file, HashMap<String, Object> params);

    Map<String, String> getConsumerFullName(ConsumerType consumerType, String consumerIinBin, String providerBin);
}
