package kz.kus.sa.tech.condition.service.report;

import kz.kus.sa.tech.condition.dto.report.epo.TechConditionEpoApplicationReportDto;
import kz.kus.sa.tech.condition.dto.report.epo.TechConditionEpoDecisionReportDto;

import java.util.UUID;

public interface TechConditionEpoReportService {

    TechConditionEpoApplicationReportDto applicationReportData(UUID id);

    TechConditionEpoDecisionReportDto getDecisionReportData(UUID id);

    TechConditionEpoDecisionReportDto getDecisionReportDataByAddress(UUID executionId, UUID abdAddressId);
}
