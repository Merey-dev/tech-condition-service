package condition.dao.repository;

import kz.kus.sa.tech.condition.dao.entity.TechConditionContractualCapacityOfTransformerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TechConditionContractualCapacityOfTransformerRepository extends JpaRepository<TechConditionContractualCapacityOfTransformerEntity, UUID> {

    List<TechConditionContractualCapacityOfTransformerEntity> findAllByTechConditionId(UUID techConditionId);
}
