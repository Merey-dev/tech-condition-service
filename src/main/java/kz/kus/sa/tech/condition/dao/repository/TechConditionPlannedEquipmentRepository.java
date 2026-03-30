package kz.kus.sa.tech.condition.dao.repository;

import kz.kus.sa.tech.condition.dao.entity.TechConditionPlannedEquipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TechConditionPlannedEquipmentRepository extends JpaRepository<TechConditionPlannedEquipmentEntity, UUID> {

    List<TechConditionPlannedEquipmentEntity> findAllByTechConditionId(UUID techConditionId);
}
