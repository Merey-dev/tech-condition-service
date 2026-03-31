package kz.kus.sa.tech.condition.service.report;

import kz.kus.sa.tech.condition.dto.report.TechConditionApplicationReportDto;
import kz.kus.sa.tech.condition.dto.report.TechConditionDecisionReportDto;

import java.util.UUID;

public interface TechConditionReportService {

    TechConditionApplicationReportDto applicationReportData(UUID id);

    TechConditionDecisionReportDto getDecisionReportData(UUID id);

    TechConditionDecisionReportDto getDecisionReportDataByAddress(UUID executionId, UUID abdAddressId);
}
