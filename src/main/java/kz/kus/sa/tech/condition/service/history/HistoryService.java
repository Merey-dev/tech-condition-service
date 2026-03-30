package kz.kus.sa.tech.condition.service.history;

import kz.kus.sa.registry.enums.Event;
import kz.kus.sa.tech.condition.dao.entity.*;
import kz.kus.sa.tech.condition.dto.history.HistoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface HistoryService {

    void save(TechConditionEntity techCondition, TechConditionExecutionEntity execution, Event event);

    void save(ActOfDelineationRenewalEntity actOfDelineationRenewal, ActOfDelineationRenewalExecutionEntity execution, Event event);

    Page<HistoryDto> getByStatementId(UUID statementId, Pageable pageable);

    List<HistoryEntity> getLast2ByTechConditionExecutionId(UUID techConditionExecutionId);
}
