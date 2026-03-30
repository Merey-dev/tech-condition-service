package kz.kus.sa.tech.condition.service.tech.condition;

import kz.kus.sa.auth.api.provider.ProviderApiService;
import kz.kus.sa.auth.api.provider.dto.EntryDto;
import kz.kus.sa.auth.api.provider.dto.ProviderDto;
import kz.kus.sa.auth.api.provider.enums.ActivityType;
import kz.kus.sa.dictionary.api.DictionaryApiService;
import kz.kus.sa.registry.api.RegistryGenerateNumberApiService;
import kz.kus.sa.registry.enums.RegistrationNumberDecisionType;
import kz.kus.sa.registry.enums.RegistrationNumberServiceType;
import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionProjectEntity;
import kz.kus.sa.tech.condition.dao.mapper.TechConditionProjectMapper;
import kz.kus.sa.tech.condition.dao.repository.TechConditionExecutionRepository;
import kz.kus.sa.tech.condition.dao.repository.TechConditionProjectRepository;
import kz.kus.sa.tech.condition.dto.project.TechConditionProjectCreateDto;
import kz.kus.sa.tech.condition.dto.project.TechConditionProjectDto;
import kz.kus.sa.tech.condition.dto.project.TechConditionProjectUpdateDto;
import kz.kus.sa.tech.condition.enums.ProjectStatus;
import kz.kus.sa.tech.condition.exception.BadRequestException;
import kz.kus.sa.tech.condition.exception.ErrorCode;
import kz.kus.sa.tech.condition.exception.NotFoundException;
import kz.kus.sa.tech.condition.service.address.AbdAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static kz.kus.sa.tech.condition.dao.specification.TechConditionProjectSpecification.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.springframework.data.jpa.domain.Specification.where;

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
