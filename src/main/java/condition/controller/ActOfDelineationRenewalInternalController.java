package condition.controller;

import kz.kus.sa.registry.dto.renewal.ActOfDelineationRenewalDto;
import kz.kus.sa.tech.condition.service.act.ActOfDelineationRenewalService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Переоформление акта разграничения балансовой принадлежности
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/internal/act-of-delineation-renewals")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ActOfDelineationRenewalInternalController {

    private final ActOfDelineationRenewalService actOfDelineationRenewalService;

    @GetMapping("/{id}")
    public ActOfDelineationRenewalDto getByStatementId(@PathVariable UUID id) {
        return actOfDelineationRenewalService.getByStatementId(id);
    }
}
