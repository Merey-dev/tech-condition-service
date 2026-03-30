package kz.kus.sa.tech.condition.controller;

import io.swagger.v3.oas.annotations.Operation;
import kz.kus.sa.registry.dto.common.SignCreateDto;
import kz.kus.sa.tech.condition.service.act.ActOfDelineationRenewalExecutionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

/**
 * Исполнения по заявлению акта разграничения балансовой принадлежности
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/act-of-delineation-renewals-executions")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ActOfDelineationRenewalExecutionController {

    private final ActOfDelineationRenewalExecutionService actOfDelineationRenewalExecutionService;

    @PatchMapping("/{statementId}/take-to-execution")
    @Operation(tags = "ACT OF DELINATION RENEWAL EVENT", summary = "Взятие акта разграничения")
    public ResponseEntity<Void> takeToExecution(@PathVariable UUID statementId) {
        actOfDelineationRenewalExecutionService.takeToExecution(statementId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{executionId}/provider-sign")
    @Operation(tags = "ACT OF DELINATION RENEWAL EXECUTION EVENT", summary = "Подписание поставщиком акта разграничения")
    public ResponseEntity<Void> providerSign(@PathVariable UUID executionId,
                                             @RequestBody @Valid SignCreateDto dto) {
        actOfDelineationRenewalExecutionService.providerSign(executionId, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{executionId}/consumer-sign")
    @Operation(tags = "ACT OF DELINATION RENEWAL EXECUTION EVENT", summary = "Подписание потребителем акта разграничения")
    public ResponseEntity<Void> consumerSign(@PathVariable UUID executionId,
                                             @RequestBody @Valid SignCreateDto dto) {
        actOfDelineationRenewalExecutionService.consumerSign(executionId, dto);
        return ResponseEntity.noContent().build();
    }
}
