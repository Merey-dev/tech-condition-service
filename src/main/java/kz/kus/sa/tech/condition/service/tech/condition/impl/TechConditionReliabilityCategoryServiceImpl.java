package kz.kus.sa.tech.condition.service.tech.condition.impl;

import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionReliabilityCategoryEntity;
import kz.kus.sa.tech.condition.dao.repository.TechConditionReliabilityCategoryRepository;
import kz.kus.sa.tech.condition.service.tech.condition.TechConditionReliabilityCategoryService;
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
public class TechConditionReliabilityCategoryServiceImpl implements TechConditionReliabilityCategoryService {

    private final TechConditionReliabilityCategoryRepository repository;

    @Override
    public void saveList(List<TechConditionReliabilityCategoryEntity> oldList,
                         List<TechConditionReliabilityCategoryEntity> newList,
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
    public List<TechConditionReliabilityCategoryEntity> getByTechConditionId(UUID techConditionId) {
        return repository.findAllByTechConditionId(techConditionId);
    }

    private void delete(List<TechConditionReliabilityCategoryEntity> list) {
        repository.deleteAll(list);
        log.info("Deleted {} TechConditionReliabilityCategories", list.size());
    }

    private void save(List<TechConditionReliabilityCategoryEntity> list) {
        repository.saveAll(list);
        log.info("Saved {} TechConditionReliabilityCategories", list.size());
    }
}
