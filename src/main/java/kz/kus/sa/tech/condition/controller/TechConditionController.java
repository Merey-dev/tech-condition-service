package kz.kus.sa.tech.condition.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import kz.kus.sa.registry.dto.common.AssignDto;
import kz.kus.sa.registry.dto.common.FileCreateDto;
import kz.kus.sa.tech.condition.dto.TechConditionDto;
import kz.kus.sa.registry.dto.tc.v1.TechConditionStatementDto;
import kz.kus.sa.tech.condition.service.tech.condition.TechConditionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

/**
 * Заявление на выдачу ТУ
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/tech-conditions")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TechConditionController {

    private final TechConditionService techConditionService;

    @Hidden
    @PostMapping
    @Operation(tags = "TC", summary = "CONSUME KAFKA")
    public ResponseEntity<TechConditionStatementDto> exampleConsume(@RequestBody @Valid TechConditionStatementDto dto) {
        return ResponseEntity.ok(techConditionService.exampleConsume(dto));
    }

    @GetMapping("/{statementId}")
    @Operation(tags = "TECH-CONDITION", summary = "Получение заявления на выдачу ТУ")
    public ResponseEntity<TechConditionDto> getByStatementId(@PathVariable UUID statementId) {
        return ResponseEntity.ok(techConditionService.getTechConditionByStatementId(statementId));
    }

    @PatchMapping("/{statementId}/assign")
    @Operation(tags = "STATEMENT EVENT", summary = "Назначение на исполнение заявления на выдачу ТУ")
    public ResponseEntity<Void> assign(@PathVariable UUID statementId,
                                       @RequestBody @Valid AssignDto dto) {
        techConditionService.assign(statementId, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{statementId}/re-assign")
    @Operation(tags = "STATEMENT EVENT", summary = "Переназначение на исполнение заявления на выдачу ТУ")
    public ResponseEntity<Void> reAssign(@PathVariable UUID statementId,
                                         @RequestBody @Valid AssignDto dto) {
        techConditionService.reAssign(statementId, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{statementId}/return-to-consumer")
    @Operation(tags = "STATEMENT EVENT", summary = "Возврат на доработку потребителю")
    public ResponseEntity<Void> returnToConsumer(@PathVariable UUID statementId,
                                                 @RequestParam @NotBlank String comment) {
        techConditionService.returnToConsumer(statementId, comment);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{statementId}/refuse-by-consumer")
    @Operation(tags = "STATEMENT EVENT", summary = "Отказ потребителя выдачу ТУ")
    public ResponseEntity<Void> refuseByConsumer(@PathVariable UUID statementId,
                                                 @RequestBody @Valid FileCreateDto dto) {
        techConditionService.refuseByConsumer(statementId, dto);
        return ResponseEntity.noContent().build();
    }
}
