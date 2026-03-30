package kz.kus.sa.tech.condition.service.tech.condition.impl;

import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionPlannedEquipmentEntity;
import kz.kus.sa.tech.condition.dao.repository.TechConditionPlannedEquipmentRepository;
import kz.kus.sa.tech.condition.service.tech.condition.TechConditionPlannedEquipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.hibernate.internal.util.collections.CollectionHelper.isNotEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class TechConditionPlannedEquipmentServiceImpl implements TechConditionPlannedEquipmentService {

    private final TechConditionPlannedEquipmentRepository repository;

    @Override
    public void saveList(List<TechConditionPlannedEquipmentEntity> oldList,
                         List<TechConditionPlannedEquipmentEntity> newList,
                         TechConditionEntity techCondition) {
        if (nonNull(techCondition) && isNotEmpty(newList)) {
            this.save(newList.stream()
                    .peek(e -> e.setTechCondition(techCondition))
                    .collect(Collectors.toList()));
            if (isNotEmpty(oldList))
                this.delete(oldList);
        }
    }

    @Override
    public List<TechConditionPlannedEquipmentEntity> getByTechConditionId(UUID techConditionId) {
        return repository.findAllByTechConditionId(techConditionId);
    }

    private void delete(List<TechConditionPlannedEquipmentEntity> list) {
        repository.deleteAll(list);
        log.info("Deleted {} TechConditionPlannedEquipments", list.size());
    }

    private void save(List<TechConditionPlannedEquipmentEntity> list) {
        repository.saveAll(list);
        log.info("Saved {} TechConditionPlannedEquipments", list.size());
    }
}
