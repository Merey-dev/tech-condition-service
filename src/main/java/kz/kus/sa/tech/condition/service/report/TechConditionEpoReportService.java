package kz.kus.sa.tech.condition.service.report;

import kz.kus.sa.tech.condition.dto.report.epo.TechConditionEpoApplicationReportDto;

import java.util.UUID;

public interface TechConditionEpoReportService {

    TechConditionEpoApplicationReportDto applicationReportData(UUID id);
}
