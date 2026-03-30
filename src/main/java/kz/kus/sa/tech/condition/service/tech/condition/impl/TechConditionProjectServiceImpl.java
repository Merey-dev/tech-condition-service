package kz.kus.sa.tech.condition.service.tech.condition.impl;

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
import kz.kus.sa.tech.condition.service.tech.condition.TechConditionProjectService;
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
import static kz.kus.sa.tech.condition.dao.specification.TechConditionProjectSpecification.byCadastral;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.springframework.data.jpa.domain.Specification.where;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TechConditionProjectServiceImpl implements TechConditionProjectService {

    private final TechConditionProjectMapper mapper;
    private final AbdAddressService abdAddressService;
    private final ProviderApiService providerApiService;
    private final TechConditionProjectRepository repository;
    private final DictionaryApiService dictionaryApiService;
    private final RegistryGenerateNumberApiService registryGenerateNumberApiService;
    private final TechConditionExecutionRepository techConditionExecutionRepository;

    @Override
    public Page<TechConditionProjectDto> getAll(String identifier, String cadastral, Pageable pageable) {
        Specification<TechConditionProjectEntity> specification = where(isNotDeleted())
                .and(byStatuses(List.of(ProjectStatus.SIGNED.getCode(), ProjectStatus.UPLOADED.getCode())))
                .and(byIdentifier(identifier))
                .and(byCadastral(cadastral));

        return repository.findAll(specification, pageable).map(mapper::toDto);
    }

    @Override
    public TechConditionProjectDto getById(UUID id) throws NotFoundException {
        return mapper.toDto(findById(id));
    }

    @Override
    public TechConditionProjectDto create(TechConditionProjectCreateDto dto) {
        return create(null, null, dto);
    }

    @Override
    public TechConditionProjectDto create(TechConditionEntity techCondition, TechConditionExecutionEntity execution, TechConditionProjectCreateDto dto) {
        TechConditionProjectEntity entity = mapper.toEntity(dto);
        return mapper.toDto(this.create(techCondition, execution, entity));
    }

    @Override
    public TechConditionProjectEntity create(TechConditionEntity techCondition, TechConditionExecutionEntity execution, TechConditionProjectEntity entity) {
        entity.setTechCondition(techCondition);
//        entity.setTechConditionExecution(execution);
        if (nonNull(techCondition)) {
            entity.setSource(techCondition.getSource());
            entity.setProviderId(techCondition.getProviderId());
        }

        entity = baseSave(entity);

        abdAddressService.saveList(null, entity.getObjectAbdAddresses(), entity);

        if (nonNull(execution)) {
            execution.setProject(entity);
            techConditionExecutionRepository.save(execution);
        }

        log.info("TECH CONDITION PROJECT [CREATED]: id = [{}]", entity.getId());
        return entity;
    }

    @Override
    public TechConditionProjectDto update(UUID id, TechConditionProjectUpdateDto dto) {
        TechConditionProjectEntity dbEntity = findById(id);
        TechConditionProjectEntity entity = mapper.toEntity(dbEntity, dto);

        abdAddressService.saveList(dbEntity.getObjectAbdAddresses(), entity.getObjectAbdAddresses(), entity);
        baseSave(entity);

        log.info("TECH CONDITION PROJECT [UPDATED]: id = [{}]", entity.getId());
        return mapper.toDto(entity);
    }

    @Override
    public void delete(UUID id) {
        TechConditionProjectEntity entity = findById(id);

        entity.setDeletedDatetime(OffsetDateTime.now());

        log.info("TECH CONDITION PROJECT [DELETED]: id = [{}]", entity.getId());
        baseSave(entity);
    }

    @Override
    public TechConditionProjectDto uploaded(UUID id) {
        TechConditionProjectEntity entity = findById(id);

        if (!Objects.equals(entity.getStatusCode(), ProjectStatus.DRAFT.getCode()))
            throw new BadRequestException("Status should be DRAFT");

        entity.setStatusCode(ProjectStatus.UPLOADED.getCode());

        log.info("TECH CONDITION PROJECT [UPLOADED]: id = [{}]", entity.getId());
        return mapper.toDto(baseSave(entity));
    }

    @Override
    public TechConditionProjectDto getByCadastralNumberOrArCode(String cadastralNumberOrArCode) {
        return mapper.toDto(repository.findByCadastralNumberOrArCode(cadastralNumberOrArCode)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name())));
    }

    @Override
    public TechConditionProjectDto generateRegistrationNumber(TechConditionProjectDto dto) {
        return mapper.toDto(this.generateRegistrationNumber(mapper.toEntity(dto)));
    }

    @Override
    public TechConditionProjectEntity generateRegistrationNumber(TechConditionProjectEntity entity) {
        if (isEmpty(entity.getRegistrationNumber())) {
            ProviderDto provider = providerApiService.getProviderDto(entity.getProviderId());
            Long nextNumber = repository.getMaxInternalRegistrationNumberByProviderIdAndYear(
                    provider.getId(), OffsetDateTime.now().getYear()) + 1;
            String activityTypeCodes = CollectionHelper.isEmpty(provider.getActivityTypeCodes())
                    ? provider.getActivityTypes().stream()
                      .map(EntryDto::getCode)
                      .distinct()
                      .sorted()
                      .collect(Collectors.joining())
                    : provider.getActivityTypeCodes().stream()
                      .map(code -> String.valueOf(ActivityType.findByCode(code)))
                      .distinct()
                      .sorted()
                      .collect(Collectors.joining());
            String value = null;
            if (isNotEmpty(entity.getOjtLocalityCode())) {
                var dictValue = dictionaryApiService.findDictionaryValueByCode(entity.getOjtLocalityCode());
                if (nonNull(dictValue)) {
                    value = String.valueOf(dictValue.getNameKz().charAt(0)).toUpperCase();
                }
            }

            entity.setInternalRegistrationNumber(nextNumber);
            entity.setRegistrationNumber(
                    registryGenerateNumberApiService.generateDecisionNumber(
                            provider.getCode(),
                            RegistrationNumberServiceType.SA,
                            RegistrationNumberDecisionType.T,
                            activityTypeCodes,
                            nextNumber,
                            value
                    )
            );
        } else if (isNotEmpty(entity.getRegistrationNumber()) && isNotEmpty(entity.getOjtLocalityCode())) {
            String value = null;
            var dictValue = dictionaryApiService.findDictionaryValueByCode(entity.getOjtLocalityCode());
            if (nonNull(dictValue)) {
                value = String.valueOf(dictValue.getNameKz().charAt(0)).toUpperCase();
            }
            var all = dictionaryApiService.findDictionaryValues(
                            entity.getOjtLocalityCode().substring(0, entity.getOjtLocalityCode().indexOf("__")),
                            null,
                            true)
                    .stream()
                    .map(e -> String.valueOf(e.getNameKz().charAt(0)).toUpperCase())
                    .collect(Collectors.joining());

            entity.setRegistrationNumber(entity.getRegistrationNumber().replaceAll("[" + all + "]", value));
        }

        log.info("TECH CONDITION PROJECT [GENERATE REGISTRATION NUMBER]: id = [{}], registration number = [{}]",
                entity.getId(), entity.getRegistrationNumber());
        return baseSave(entity);
    }

    private TechConditionProjectEntity baseSave(TechConditionProjectEntity entity) {
        OffsetDateTime now = OffsetDateTime.now();
        if (isNull(entity.getCreatedDatetime())) {
            entity.setCreatedDatetime(now);
        }
        entity.setLastModifiedDatetime(now);
        return repository.save(entity);
    }

    private TechConditionProjectEntity findById(UUID id) {
        return repository.findByIdAndDeletedDatetimeIsNull(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }
}
