package kz.kus.sa.tech.condition.controller;

import io.swagger.v3.oas.annotations.Operation;
import kz.kus.sa.tech.condition.dto.report.TechConditionApplicationReportDto;
import kz.kus.sa.tech.condition.dto.report.TechConditionDecisionReportDto;
import kz.kus.sa.tech.condition.dto.report.epo.TechConditionEpoApplicationReportDto;
import kz.kus.sa.tech.condition.dto.report.epo.TechConditionEpoDecisionReportDto;
import kz.kus.sa.tech.condition.service.report.TechConditionEpoReportService;
import kz.kus.sa.tech.condition.service.report.TechConditionReportService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Бланки ТУ
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/internal/tech-conditions")
public class ReportInternalController {

    private final TechConditionReportService techConditionReportService;
    private final TechConditionEpoReportService techConditionEpoReportService;

    @GetMapping("/{id}/application-report")
    @Operation(tags = "TECH CONDITION BLANK", summary = "Данные для бланка заявление ТУ")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TechConditionApplicationReportDto> applicationReportData(@PathVariable UUID id) {
        return ResponseEntity.ok(techConditionReportService.applicationReportData(id));
    }

    @GetMapping("/{id}/decision-report")
    @Operation(tags = "TECH CONDITION BLANK", summary = "Данные для решения ТУ")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TechConditionDecisionReportDto> getDecisionReportData(@PathVariable UUID id) {
        return ResponseEntity.ok(techConditionReportService.getDecisionReportData(id));
    }

    @GetMapping("/{id}/decision-report/address/{abdAddressId}")
    @Operation(tags = "TECH CONDITION BLANK", summary = "Данные для решения ТУ по конкретному адресу")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TechConditionDecisionReportDto> getDecisionReportDataByAddress(@PathVariable UUID id,
                                                                                         @PathVariable UUID abdAddressId) {
        return ResponseEntity.ok(techConditionReportService.getDecisionReportDataByAddress(id, abdAddressId));
    }

    @GetMapping("/epo/{id}/application-report")
    @Operation(tags = "TECH CONDITION EPO BLANK", summary = "Данные для бланка заявление Согласование ТУ по сетям ЭПО")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TechConditionEpoApplicationReportDto> applicationEpoReportData(@PathVariable UUID id) {
        return ResponseEntity.ok(techConditionEpoReportService.applicationReportData(id));
    }

    @GetMapping("/epo/{id}/decision-report")
    @Operation(tags = "TECH CONDITION EPO BLANK", summary = "Данные для решения Согласование ТУ по сетям ЭПО")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TechConditionEpoDecisionReportDto> getDecisionEpoReportData(@PathVariable UUID id) {
        return ResponseEntity.ok(techConditionEpoReportService.getDecisionReportData(id));
    }

    @GetMapping("/epo/{id}/decision-report/address/{abdAddressId}")
    @Operation(tags = "TECH CONDITION EPO BLANK", summary = "Данные для решения Согласование ТУ по сетям ЭПО по конкретному адресу")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TechConditionEpoDecisionReportDto> getDecisionEpoReportDataByAddress(@PathVariable UUID id,
                                                                                               @PathVariable UUID abdAddressId) {
        return ResponseEntity.ok(techConditionEpoReportService.getDecisionReportDataByAddress(id, abdAddressId));
    }
}
