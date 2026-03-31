package kz.kus.sa.tech.condition.service.tech.condition;

import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionProjectEntity;
import kz.kus.sa.tech.condition.dto.project.TechConditionProjectCreateDto;
import kz.kus.sa.tech.condition.dto.project.TechConditionProjectDto;
import kz.kus.sa.tech.condition.dto.project.TechConditionProjectUpdateDto;
import kz.kus.sa.tech.condition.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TechConditionProjectService {

    Page<TechConditionProjectDto> getAll(String identifier, String cadastral, Pageable pageable);

    TechConditionProjectDto getById(UUID id) throws NotFoundException;

    TechConditionProjectDto create(TechConditionProjectCreateDto dto);

    TechConditionProjectDto create(TechConditionEntity techCondition, TechConditionExecutionEntity execution, TechConditionProjectCreateDto dto);

    TechConditionProjectEntity create(TechConditionEntity techCondition, TechConditionExecutionEntity execution, TechConditionProjectEntity entity);

    TechConditionProjectDto update(UUID id, TechConditionProjectUpdateDto dto);

    void delete(UUID id);

    TechConditionProjectDto uploaded(UUID id);

    TechConditionProjectDto getByCadastralNumberOrArCode(String cadastralNumberOrArCode);

    TechConditionProjectDto generateRegistrationNumber(TechConditionProjectDto dto);

    TechConditionProjectEntity generateRegistrationNumber(TechConditionProjectEntity entity);
}
