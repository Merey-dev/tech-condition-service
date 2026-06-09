package kz.kus.sa.tech.condition.controller;

import io.swagger.v3.oas.annotations.Operation;
import kz.kus.sa.registry.dto.common.AssignDto;
import kz.kus.sa.registry.dto.common.FileCreateDto;
import kz.kus.sa.tech.condition.service.epo.TechConditionEpoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

/**
 * Заявление на Согласование ТУ по сетям ЭПО
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/tech-conditions/epo")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TechConditionEpoController {
    
    private final TechConditionEpoService techConditionEpoService;

    @PatchMapping("/{statementId}/assign")
    @Operation(tags = "STATEMENT EVENT", summary = "Назначение на исполнение заявления на Согласование ТУ по сетям ЭПО")
    public ResponseEntity<Void> assign(@PathVariable UUID statementId,
                                       @RequestBody @Valid AssignDto dto) {
        techConditionEpoService.assign(statementId, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{statementId}/re-assign")
    @Operation(tags = "STATEMENT EVENT", summary = "Переназначение на исполнение заявления на Согласование ТУ по сетям ЭПО")
    public ResponseEntity<Void> reAssign(@PathVariable UUID statementId,
                                         @RequestBody @Valid AssignDto dto) {
        techConditionEpoService.reAssign(statementId, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{statementId}/return-to-consumer")
    @Operation(tags = "STATEMENT EVENT", summary = "Возврат на доработку потребителю")
    public ResponseEntity<Void> returnToConsumer(@PathVariable UUID statementId,
                                                 @RequestParam @NotBlank String comment) {
        techConditionEpoService.returnToConsumer(statementId, comment);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{statementId}/refuse-by-consumer")
    @Operation(tags = "STATEMENT EVENT", summary = "Отказ потребителя Согласование ТУ по сетям ЭПО")
    public ResponseEntity<Void> refuseByConsumer(@PathVariable UUID statementId,
                                                 @RequestBody @Valid FileCreateDto dto) {
        techConditionEpoService.refuseByConsumer(statementId, dto);
        return ResponseEntity.noContent().build();
    }
}
