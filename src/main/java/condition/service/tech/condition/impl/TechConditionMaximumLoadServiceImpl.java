package condition.service.tech.condition.impl;

import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionMaximumLoadEntity;
import kz.kus.sa.tech.condition.dao.repository.TechConditionMaximumLoadRepository;
import kz.kus.sa.tech.condition.service.tech.condition.TechConditionMaximumLoadService;
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
public class TechConditionMaximumLoadServiceImpl implements TechConditionMaximumLoadService {

    private final TechConditionMaximumLoadRepository repository;

    @Override
    public void saveList(List<TechConditionMaximumLoadEntity> oldList,
                         List<TechConditionMaximumLoadEntity> newList,
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
    public List<TechConditionMaximumLoadEntity> getByTechConditionId(UUID techConditionId) {
        return repository.findAllByTechConditionId(techConditionId);
    }

    private void delete(List<TechConditionMaximumLoadEntity> list) {
        repository.deleteAll(list);
        log.info("Deleted {} TechConditionMaximumLoads", list.size());
    }

    private void save(List<TechConditionMaximumLoadEntity> list) {
        repository.saveAll(list);
        log.info("Saved {} TechConditionMaximumLoads", list.size());
    }
}
