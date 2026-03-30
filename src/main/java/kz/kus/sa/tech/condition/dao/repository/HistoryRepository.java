package kz.kus.sa.tech.condition.dao.repository;

import kz.kus.sa.tech.condition.dao.entity.HistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistoryRepository extends JpaRepository<HistoryEntity, UUID> {

    @Query(value = "select h from HistoryEntity h " +
            "where h.techCondition.statementId = ?1 " +
            "  and h.registryType = 'STATEMENT' order by h.eventDatetime")
    List<HistoryEntity> findAllByStatementIdOrderByEventDatetime(UUID statementId);

    List<HistoryEntity> findLast2ByTechConditionExecutionId(UUID techConditionExecutionId);
}
