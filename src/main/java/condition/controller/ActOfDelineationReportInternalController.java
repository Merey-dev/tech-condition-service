package condition.controller;

import io.swagger.v3.oas.annotations.Operation;
import kz.kus.sa.tech.condition.dto.report.act.ActOfDelineationRenewalApplicationReportDto;
import kz.kus.sa.tech.condition.dto.report.act.ActOfDelineationRenewalReportDto;
import kz.kus.sa.tech.condition.service.report.ActOfDelineationReportService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Бланки Акт разграничения балансовой принадлежности
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/internal/act-of-delineations")
public class ActOfDelineationReportInternalController {

    private final ActOfDelineationReportService reportService;

    @GetMapping("/{id}/application-report")
    @Operation(tags = "ACT OF DELINATION RENEWAL BLANK", summary = "Данные для заявление акцепта Переоформление акта разграничения")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActOfDelineationRenewalApplicationReportDto> applicationReportData(@PathVariable UUID id) {
        return ResponseEntity.ok(reportService.applicationReportData(id));
    }

    @GetMapping("/{id}/act-report")
    @Operation(tags = "ACT OF DELINATION RENEWAL BLANK", summary = "Данные для бланка Акта разграничения")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActOfDelineationRenewalReportDto> actReportData(@PathVariable UUID id) {
        return ResponseEntity.ok(reportService.actReportData(id));
    }
}
