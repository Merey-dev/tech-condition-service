package condition.service.report;

import kz.kus.sa.tech.condition.dto.report.act.ActOfDelineationRenewalApplicationReportDto;
import kz.kus.sa.tech.condition.dto.report.act.ActOfDelineationRenewalReportDto;

import java.util.UUID;

public interface ActOfDelineationReportService {

    ActOfDelineationRenewalApplicationReportDto applicationReportData(UUID id);

    ActOfDelineationRenewalReportDto actReportData(UUID id);
}
