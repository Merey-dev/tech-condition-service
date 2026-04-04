package condition.controller;

import io.swagger.v3.oas.annotations.Operation;
import kz.kus.sa.registry.dto.common.AssignDto;
import kz.kus.sa.tech.condition.service.act.ActOfDelineationRenewalService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
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

    @PatchMapping("/{statementId}/assign")
    @Operation(tags = "ACT OF DELINATION RENEWAL EVENT", summary = "Назначение акта разграничения")
    public ResponseEntity<Void> assign(@PathVariable UUID statementId,
                                       @RequestBody @Valid AssignDto dto) {
        actOfDelineationRenewalService.assign(statementId, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{statementId}/take-to-execution")
    @Operation(tags = "ACT OF DELINATION RENEWAL EVENT", summary = "Взятие акта разграничения")
    public ResponseEntity<Void> takeToExecution(@PathVariable UUID statementId) {
        actOfDelineationRenewalService.takeToExecution(statementId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{statementId}/send-for-approval")
    @Operation(tags = "ACT OF DELINATION RENEWAL EVENT", summary = "Отправка на согласование акта разграничения")
    public ResponseEntity<Void> sendForApproval(@PathVariable UUID statementId,
                                                @RequestBody @Valid AssignDto dto) {
        actOfDelineationRenewalService.sendForApproval(statementId, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{statementId}/send-for-confirmation")
    @Operation(tags = "ACT OF DELINATION RENEWAL EVENT", summary = "Отправка на утверждение акта разграничения")
    public ResponseEntity<Void> sendForConfirmation(@PathVariable UUID statementId,
                                                    @RequestBody @Valid AssignDto dto) {
        actOfDelineationRenewalService.sendForConfirmation(statementId, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{statementId}/send-for-revision")
    @Operation(tags = "ACT OF DELINATION RENEWAL EVENT", summary = "Отправка на доработку акта разграничения")
    public ResponseEntity<Void> sendForRevision(@PathVariable UUID statementId,
                                                @RequestBody @Valid AssignDto dto,
                                                @RequestParam @NotBlank String reason) {
        actOfDelineationRenewalService.sendForRevision(statementId, dto, reason);
        return ResponseEntity.noContent().build();
    }

    /*@PatchMapping("/{statementId}/provider-sign")
    @Operation(tags = "ACT OF DELINATION RENEWAL EVENT", summary = "Подписание поставщиком акта разграничения")
    public ResponseEntity<Void> providerSign(@PathVariable UUID statementId,
                                             @RequestBody @Valid SignCreateDto dto) {
        actOfDelineationRenewalService.providerSign(statementId, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{statementId}/consumer-sign")
    @Operation(tags = "ACT OF DELINATION RENEWAL EVENT", summary = "Подписание потребителем акта разграничения")
    public ResponseEntity<Void> consumerSign(@PathVariable UUID statementId,
                                             @RequestBody @Valid SignCreateDto dto) {
        actOfDelineationRenewalService.consumerSign(statementId, dto);
        return ResponseEntity.noContent().build();
    }*/
}
