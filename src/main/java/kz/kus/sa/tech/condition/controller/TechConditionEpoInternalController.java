package kz.kus.sa.tech.condition.controller;

import kz.kus.sa.registry.dto.tc.epo.TechConditionEpoStatementDto;
import kz.kus.sa.tech.condition.service.epo.TechConditionEpoService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Заявление на Согласование ТУ по сетям ЭПО
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/internal/tech-conditions/epo")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TechConditionEpoInternalController {

    private final TechConditionEpoService techConditionEpoService;

    @GetMapping("/{id}")
    public TechConditionEpoStatementDto getByStatementId(@PathVariable UUID id) {
        return techConditionEpoService.getByStatementId(id);
    }
}
