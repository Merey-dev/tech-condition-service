package condition.controller;

import io.swagger.v3.oas.annotations.Operation;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationCreateDto;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationDto;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationSearchDto;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationUpdateDto;
import kz.kus.sa.tech.condition.service.act.ActOfDelineationService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

/**
 * Акт разграничения балансовой принадлежности
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/act-of-delineations")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ActOfDelineationController {

    private final ActOfDelineationService actOfDelineationService;

    @PostMapping("/search")
    @Operation(tags = "ACT OF DELINEATION", summary = "Список актов")
    public ResponseEntity<Page<ActOfDelineationDto>> getAll(@RequestBody ActOfDelineationSearchDto search, Pageable pageable) {
        return ResponseEntity.ok(actOfDelineationService.getAll(search, pageable));
    }

    @PostMapping
    @Operation(tags = "ACT OF DELINEATION", summary = "Создание акта")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ActOfDelineationDto> create(@RequestBody @Valid ActOfDelineationCreateDto dto) {
        return ResponseEntity.ok(actOfDelineationService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(tags = "ACT OF DELINEATION", summary = "Редактирование акта")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ActOfDelineationDto> update(@PathVariable UUID id,
                                                      @RequestBody @Valid ActOfDelineationUpdateDto dto) {
        return ResponseEntity.ok(actOfDelineationService.update(id, dto));
    }

    @GetMapping("/{id}")
    @Operation(tags = "ACT OF DELINEATION", summary = "Получение акта по ID")
    public ResponseEntity<ActOfDelineationDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(actOfDelineationService.getById(id));
    }
}
