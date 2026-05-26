package kz.kus.sa.tech.condition.controller;

import io.swagger.v3.oas.annotations.Operation;
import kz.kus.sa.registry.dto.common.AssignDto;
import kz.kus.sa.registry.dto.common.SignCreateDto;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationRenewalAbdAddressDecisionDto;
import kz.kus.sa.tech.condition.service.act.ActOfDelineationRenewalAbdAddressDecisionService;
import kz.kus.sa.tech.condition.service.act.ActOfDelineationRenewalService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

/**
 * Переоформление акта разграничения балансовой принадлежности
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/act-of-delineation-renewals")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ActOfDelineationRenewalController {

    private final ActOfDelineationRenewalService actOfDelineationRenewalService;
    private final ActOfDelineationRenewalAbdAddressDecisionService decisionService;

    @PatchMapping("/{statementId}/assign")
    @Operation(tags = "ACT OF DELINATION RENEWAL EVENT", summary = "Назначение на исполнение")
    public ResponseEntity<Void> assign(@PathVariable UUID statementId,
                                       @RequestBody @Valid AssignDto dto) {
        actOfDelineationRenewalService.assign(statementId, dto);
        return ResponseEntity.noContent().build();
    }

    // ─── СПИСОК DECISIONS ПО ЗАЯВЛЕНИЮ ────────────────────────────────────

    @GetMapping("/{renewalId}/decisions")
    @Operation(tags = "ACT OF DELINATION RENEWAL DECISIONS", summary = "Список decisions заявления (по адресам)")
    public ResponseEntity<List<ActOfDelineationRenewalAbdAddressDecisionDto>> getDecisions(
            @PathVariable UUID renewalId) {
        return ResponseEntity.ok(decisionService.getAllByRenewalId(renewalId));
    }

    @GetMapping("/decisions/{decisionId}")
    @Operation(tags = "ACT OF DELINATION RENEWAL DECISIONS", summary = "Получение decision по ID")
    public ResponseEntity<ActOfDelineationRenewalAbdAddressDecisionDto> getDecisionById(
            @PathVariable UUID decisionId) {
        return ResponseEntity.ok(decisionService.getById(decisionId));
    }

    // ─── PER-ADDRESS DECISION EVENTS ──────────────────────────────────────

    @PatchMapping("/decisions/{decisionId}/take-to-execution")
    @Operation(tags = "ACT OF DELINATION RENEWAL DECISION EVENTS", summary = "Взятие в работу по адресу")
    public ResponseEntity<Void> takeDecisionToExecution(@PathVariable UUID decisionId) {
        decisionService.takeToExecution(decisionId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/decisions/{decisionId}/send-for-approval")
    @Operation(tags = "ACT OF DELINATION RENEWAL DECISION EVENTS", summary = "Отправка на согласование")
    public ResponseEntity<Void> sendDecisionForApproval(@PathVariable UUID decisionId,
                                                       @RequestBody @Valid AssignDto dto) {
        decisionService.sendForApproval(decisionId, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/decisions/{decisionId}/send-for-confirmation")
    @Operation(tags = "ACT OF DELINATION RENEWAL DECISION EVENTS", summary = "Отправка на утверждение (директору)")
    public ResponseEntity<Void> sendDecisionForConfirmation(@PathVariable UUID decisionId,
                                                            @RequestBody @Valid AssignDto dto) {
        decisionService.sendForConfirmation(decisionId, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/decisions/{decisionId}/send-for-revision")
    @Operation(tags = "ACT OF DELINATION RENEWAL DECISION EVENTS", summary = "Отправка на доработку")
    public ResponseEntity<Void> sendDecisionForRevision(@PathVariable UUID decisionId,
                                                        @RequestParam @NotBlank String reason) {
        decisionService.sendForRevision(decisionId, reason);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/decisions/{decisionId}/provider-sign")
    @Operation(tags = "ACT OF DELINATION RENEWAL DECISION EVENTS", summary = "Подписание поставщиком")
    public ResponseEntity<Void> providerSignDecision(@PathVariable UUID decisionId,
                                                     @RequestBody @Valid SignCreateDto dto) {
        decisionService.providerSign(decisionId, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/decisions/{decisionId}/consumer-sign")
    @Operation(tags = "ACT OF DELINATION RENEWAL DECISION EVENTS", summary = "Подписание потребителем")
    public ResponseEntity<Void> consumerSignDecision(@PathVariable UUID decisionId,
                                                     @RequestBody @Valid SignCreateDto dto) {
        decisionService.consumerSign(decisionId, dto);
        return ResponseEntity.noContent().build();
    }
}
