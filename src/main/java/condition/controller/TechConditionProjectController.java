package condition.controller;

import io.swagger.v3.oas.annotations.Operation;
import kz.kus.sa.tech.condition.dto.project.TechConditionProjectCreateDto;
import kz.kus.sa.tech.condition.dto.project.TechConditionProjectDto;
import kz.kus.sa.tech.condition.dto.project.TechConditionProjectUpdateDto;
import kz.kus.sa.tech.condition.service.tech.condition.TechConditionProjectService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

/**
 * Проект ТУ
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/tech-conditions-projects")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TechConditionProjectController {

    private final TechConditionProjectService techConditionProjectService;

    @GetMapping
    @Operation(tags = "TECH-CONDITION PROJECT", summary = "Список проектов ТУ")
    public Page<TechConditionProjectDto> getAll(@RequestParam(required = false) String identifier,
                                                @RequestParam(required = false) String cadastral,
                                                Pageable pageable) {
        return techConditionProjectService.getAll(identifier, cadastral, pageable);
    }

    @GetMapping("/{id}")
    @Operation(tags = "TECH-CONDITION PROJECT", summary = "Получение проекта по ID")
    public TechConditionProjectDto getById(@PathVariable UUID id) {
        return techConditionProjectService.getById(id);
    }

    @PostMapping
    @Operation(tags = "TECH-CONDITION PROJECT", summary = "Создание проекта ТУ")
    @ResponseStatus(HttpStatus.CREATED)
    public TechConditionProjectDto create(@RequestBody @Valid TechConditionProjectCreateDto dto) {
        return techConditionProjectService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(tags = "TECH-CONDITION PROJECT", summary = "Редактирование проекта ТУ")
    @ResponseStatus(HttpStatus.OK)
    public TechConditionProjectDto update(@PathVariable UUID id,
                                          @RequestBody @Valid TechConditionProjectUpdateDto dto) {
        return techConditionProjectService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(tags = "TECH-CONDITION PROJECT", summary = "Удаление проекта ТУ")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        techConditionProjectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/uploaded")
    @Operation(tags = "TECH-CONDITION PROJECT", summary = "Изменить статус проекта ТУ на Загружен")
    @ResponseStatus(HttpStatus.OK)
    public TechConditionProjectDto update(@PathVariable UUID id) {
        return techConditionProjectService.uploaded(id);
    }

    @GetMapping("/by-cadastral-number-or-ar-code")
    @Operation(tags = "TECH-CONDITION PROJECT", summary = "Получение проекта ТУ по кадастровому номеру или АР код")
    @ResponseStatus(HttpStatus.OK)
    public TechConditionProjectDto getByCadastralNumberOrArCode(@RequestParam String cadastralNumberOrArCode) {
        return techConditionProjectService.getByCadastralNumberOrArCode(cadastralNumberOrArCode);
    }
}
