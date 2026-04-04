package condition.service.tech.condition.impl;

import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionSubConsumerEntity;
import kz.kus.sa.tech.condition.dao.repository.TechConditionSubConsumerRepository;
import kz.kus.sa.tech.condition.service.tech.condition.TechConditionSubConsumerService;
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
public class TechConditionSubConsumerServiceImpl implements TechConditionSubConsumerService {

    private final TechConditionSubConsumerRepository repository;

    @Override
    public void saveList(List<TechConditionSubConsumerEntity> oldList,
                         List<TechConditionSubConsumerEntity> newList,
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
    public List<TechConditionSubConsumerEntity> getByTechConditionId(UUID techConditionId) {
        return repository.findAllByTechConditionId(techConditionId);
    }

    private void delete(List<TechConditionSubConsumerEntity> list) {
        repository.deleteAll(list);
        log.info("Deleted {} TechConditionSubConsumers", list.size());
    }

    private void save(List<TechConditionSubConsumerEntity> list) {
        repository.saveAll(list);
        log.info("Saved {} TechConditionSubConsumers", list.size());
    }
}
