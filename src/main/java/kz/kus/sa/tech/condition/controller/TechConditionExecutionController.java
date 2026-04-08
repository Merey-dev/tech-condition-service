package kz.kus.sa.tech.condition.controller;

import io.swagger.v3.oas.annotations.Operation;
import kz.kus.sa.registry.dto.common.AssignDto;
import kz.kus.sa.registry.dto.common.SignCreateDto;
import kz.kus.sa.registry.enums.Source;
import kz.kus.sa.tech.condition.dto.ChangeAssigneeDto;
import kz.kus.sa.tech.condition.dto.TechConditionExecuteDto;
import kz.kus.sa.tech.condition.dto.execution.TechConditionExecutionDto;
import kz.kus.sa.tech.condition.dto.project.TechConditionProjectCreateDto;
import kz.kus.sa.tech.condition.dto.project.TechConditionProjectDto;
import kz.kus.sa.tech.condition.service.tech.condition.TechConditionExecutionAbdAddressDecisionService;
import kz.kus.sa.tech.condition.service.tech.condition.TechConditionExecutionService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Исполнения по заявлению на выдачу ТУ
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/tech-conditions-executions")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TechConditionExecutionController {
    
    private final TechConditionExecutionService techConditionExecutionService;
    private final TechConditionExecutionAbdAddressDecisionService techConditionExecutionAbdAddressDecisionService;

    /** Администрирование */
    @GetMapping("/admin")
    @Operation(tags = "TECH-CONDITION ADMIN", summary = "Админка: Список исполнении по заявлениям")
    public Page<TechConditionExecutionDto> getAllForAdmin(
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) List<String> statementStatuses,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate dateTo,
            @RequestParam(required = false) Source source,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID providerId,
            Pageable pageable) {
        return techConditionExecutionService.getAllForAdmin(searchText, statuses, statementStatuses,
                dateFrom, dateTo, source, userId, providerId, pageable);
    }

    @PatchMapping("/admin/change-assignee")
    @Operation(tags = "TECH-CONDITION ADMIN", summary = "Админка: Изменение исполнителя")
    public ResponseEntity<Void> changeAssignee(@RequestBody @Valid ChangeAssigneeDto dto) {
        techConditionExecutionService.changeAssignee(dto);
        return ResponseEntity.noContent().build();
    }

    /** Лист исполнения */
    @GetMapping
    @Operation(tags = "TECH-CONDITION EXECUTION", summary = "Список исполнении по заявлениям")
    public Page<TechConditionExecutionDto> getAll(
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) List<String> statementStatuses,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate dateTo,
            @RequestParam(required = false) Source source,
            @RequestParam(required = false) UUID userId,
            Pageable pageable) {
        return techConditionExecutionService.getAll(searchText, statuses, statementStatuses,
                dateFrom, dateTo, source, userId, pageable);
    }

    @GetMapping("/{id}")
    @Operation(tags = "TECH-CONDITION EXECUTION", summary = "Получение исполнения по ID")
    public TechConditionExecutionDto getById(@PathVariable UUID id) {
        return techConditionExecutionService.getById(id);
    }

    @GetMapping("/{techConditionId}/executions")
    @Operation(tags = "TECH-CONDITION EXECUTION", summary = "Список исполнении к заявлению (Pageable)")
    public Page<TechConditionExecutionDto> getExecutions(@PathVariable UUID techConditionId,
                                                         Pageable pageable) {
        return techConditionExecutionService.findAllByTechConditionId(techConditionId, pageable);
    }

    @GetMapping("/{techConditionId}/executions-list")
    @Operation(tags = "TECH-CONDITION EXECUTION", summary = "Список исполнении к заявлению (List)")
    public List<TechConditionExecutionDto> getExecutionsList(@PathVariable UUID techConditionId) {
        return techConditionExecutionService.findAllByTechConditionIdList(techConditionId);
    }

    @PatchMapping("/{id}/take-to-execution")
    @Operation(tags = "TECH-CONDITION EXECUTION EVENTS", summary = "Взятие на исполнение")
    public ResponseEntity<Void> takeToExecution(@PathVariable UUID id,
                                                @RequestBody @Valid AssignDto dto) {
        techConditionExecutionService.takeToExecution(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/assign")
    @Operation(tags = "TECH-CONDITION EXECUTION EVENTS", summary = "Назначение исполнение")
    public ResponseEntity<Void> assign(@PathVariable UUID id,
                                       @RequestBody @Valid AssignDto assignDto) {
        techConditionExecutionService.assign(id, assignDto);
        return ResponseEntity.noContent().build();
    }

    /** Проект ТУ или мотивированный отказ */
    @PostMapping("/{id}/decision/project")
    @Operation(tags = "TECH-CONDITION EXECUTION EVENTS", summary = "Формирование проекта ТУ")
    public TechConditionProjectDto createProject(@PathVariable UUID id,
                                                 @RequestBody @Valid TechConditionProjectCreateDto dto) {
        return techConditionExecutionAbdAddressDecisionService.createProject(id, dto);
    }

    @PatchMapping("/{id}/decision/formation-reasoned-refusal")
    @Operation(tags = "TECH-CONDITION EXECUTION EVENTS", summary = "Формирование мотивированного отказа")
    public ResponseEntity<Void> formationReasonedRefusal(@PathVariable UUID id,
                                                         @RequestBody @Valid TechConditionExecuteDto dto) {
        techConditionExecutionAbdAddressDecisionService.formationReasonedRefusal(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/execute-application")
    @Operation(tags = "TECH-CONDITION EXECUTION EVENTS", summary = "Исполнение")
    public ResponseEntity<Void> executeApplication(@PathVariable UUID id,
                                                   @RequestBody @Valid TechConditionExecuteDto dto) {
        techConditionExecutionAbdAddressDecisionService.executeApplication(id, dto);
        return ResponseEntity.noContent().build();
    }

    /** Decision события */
    @PatchMapping("/decisions/{decisionId}/take-to-execution")
    @Operation(tags = "TECH-CONDITION DECISION EVENTS", summary = "Взятие в работу решения по адресу")
    public ResponseEntity<Void> takeDecisionToExecution(@PathVariable UUID decisionId) {
        techConditionExecutionAbdAddressDecisionService.takeToExecution(decisionId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/decisions/{decisionId}/send-for-revision")
    @Operation(tags = "TECH-CONDITION DECISION EVENTS", summary = "Отправка решения на доработку")
    public ResponseEntity<Void> sendDecisionForRevision(@PathVariable UUID decisionId,
                                                        @RequestParam @NotBlank String reason) {
        techConditionExecutionAbdAddressDecisionService.sendForRevision(decisionId, reason);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/decisions/{decisionId}/send-for-approval")
    @Operation(tags = "TECH-CONDITION DECISION EVENTS", summary = "Отправка решения на согласование")
    public ResponseEntity<Void> sendDecisionForApproval(@PathVariable UUID decisionId,
                                                        @RequestBody @Valid AssignDto dto) {
        techConditionExecutionAbdAddressDecisionService.sendForApproval(decisionId, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/decisions/{decisionId}/approve")
    @Operation(tags = "TECH-CONDITION DECISION EVENTS", summary = "Согласование решения")
    public ResponseEntity<Void> approveDecision(@PathVariable UUID decisionId) {
        techConditionExecutionAbdAddressDecisionService.approve(decisionId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/decisions/{decisionId}/send-for-sign")
    @Operation(tags = "TECH-CONDITION DECISION EVENTS", summary = "Отправка решения на подписание")
    public ResponseEntity<Void> sendDecisionForSign(@PathVariable UUID decisionId,
                                                    @RequestBody @Valid AssignDto dto) {
        techConditionExecutionAbdAddressDecisionService.sendForSign(decisionId, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/decisions/{decisionId}/approve-and-send-for-sign")
    @Operation(tags = "TECH-CONDITION DECISION EVENTS", summary = "Согласование и отправка на подписание")
    public ResponseEntity<Void> approveDecisionAndSendForSign(@PathVariable UUID decisionId,
                                                              @RequestBody @Valid AssignDto dto) {
        techConditionExecutionAbdAddressDecisionService.approveAndSendForSign(decisionId, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/decisions/{decisionId}/sign")
    @Operation(tags = "TECH-CONDITION DECISION EVENTS", summary = "Подписание решения")
    public ResponseEntity<Void> signDecision(@PathVariable UUID decisionId,
                                             @RequestBody SignCreateDto sign) {
        techConditionExecutionAbdAddressDecisionService.sign(decisionId, sign);
        return ResponseEntity.noContent().build();
    }
}
