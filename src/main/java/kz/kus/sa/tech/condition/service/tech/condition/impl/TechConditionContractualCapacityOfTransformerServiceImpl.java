package kz.kus.sa.tech.condition.service.tech.condition.impl;

import kz.kus.sa.tech.condition.dao.entity.TechConditionContractualCapacityOfTransformerEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.repository.TechConditionContractualCapacityOfTransformerRepository;
import kz.kus.sa.tech.condition.service.tech.condition.TechConditionContractualCapacityOfTransformerService;
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
public class TechConditionContractualCapacityOfTransformerServiceImpl implements TechConditionContractualCapacityOfTransformerService {

    private final TechConditionContractualCapacityOfTransformerRepository repository;

    @Override
    public void saveList(List<TechConditionContractualCapacityOfTransformerEntity> oldList,
                         List<TechConditionContractualCapacityOfTransformerEntity> newList,
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
    public List<TechConditionContractualCapacityOfTransformerEntity> getByTechConditionId(UUID techConditionId) {
        return repository.findAllByTechConditionId(techConditionId);
    }

    private void delete(List<TechConditionContractualCapacityOfTransformerEntity> list) {
        repository.deleteAll(list);
        log.info("Deleted {} TechConditionContractualCapacityOfTransformers", list.size());
    }

    private void save(List<TechConditionContractualCapacityOfTransformerEntity> list) {
        repository.saveAll(list);
        log.info("Saved {} TechConditionContractualCapacityOfTransformers", list.size());
    }
}
