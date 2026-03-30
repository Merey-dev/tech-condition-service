package kz.kus.sa.tech.condition.controller;

import kz.kus.sa.registry.dto.tc.v1.TechConditionStatementDto;
import kz.kus.sa.tech.condition.service.tech.condition.TechConditionService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Заявление на выдачу ТУ
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/internal/tech-conditions")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TechConditionInternalController {

    private final TechConditionService techConditionService;

    @GetMapping("/{id}")
    public TechConditionStatementDto getByStatementId(@PathVariable UUID id) {
        return techConditionService.getByStatementId(id);
    }
}
