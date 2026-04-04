package condition.service.address.impl;

import kz.kus.sa.tech.condition.dao.entity.IntersectionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionProjectEntity;
import kz.kus.sa.tech.condition.dao.repository.IntersectionRepository;
import kz.kus.sa.tech.condition.service.address.IntersectionService;
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
public class IntersectionServiceImpl implements IntersectionService {

    private final IntersectionRepository repository;

    @Override
    public void saveList(List<IntersectionEntity> oldList, List<IntersectionEntity> newList, Object object) {
        if (nonNull(object) && isNotEmpty(newList)) {
            if (object instanceof TechConditionEntity)
                this.save(newList.stream()
                        .peek(e -> e.setTechCondition((TechConditionEntity) object))
                        .collect(Collectors.toList()));
            else if (object instanceof TechConditionExecutionEntity)
                this.save(newList.stream()
                        .peek(e -> e.setTechConditionExecution((TechConditionExecutionEntity) object))
                        .collect(Collectors.toList()));
            else if (object instanceof TechConditionProjectEntity)
                this.save(newList.stream()
                        .peek(e -> e.setTechConditionProject((TechConditionProjectEntity) object))
                        .collect(Collectors.toList()));
            if (isNotEmpty(oldList))
                this.delete(oldList);
        }
    }

    @Override
    public List<IntersectionEntity> getByTechConditionId(UUID techConditionId) {
        return repository.findAllByTechConditionId(techConditionId);
    }

    @Override
    public List<IntersectionEntity> getByTechConditionExecutionId(UUID techConditionExecutionId) {
        return repository.findAllByTechConditionExecutionId(techConditionExecutionId);
    }

    @Override
    public List<IntersectionEntity> getByTechConditionProjectId(UUID techConditionProjectId) {
        return repository.findAllByTechConditionProjectId(techConditionProjectId);
    }

    private void delete(List<IntersectionEntity> list) {
        repository.deleteAll(list);
        log.info("Deleted {} Intersections", list.size());
    }

    private void save(List<IntersectionEntity> list) {
        repository.saveAll(list);
        log.info("Saved {} Intersections", list.size());
    }
}
