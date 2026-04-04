package condition.service.act.impl;

import kz.kus.sa.auth.api.provider.ProviderApiService;
import kz.kus.sa.auth.api.provider.dto.EntryDto;
import kz.kus.sa.auth.api.provider.enums.ActivityType;
import kz.kus.sa.registry.api.RegistryGenerateNumberApiService;
import kz.kus.sa.registry.enums.RegistrationNumberActType;
import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationEntity;
import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationRenewalExecutionEntity;
import kz.kus.sa.tech.condition.dao.mapper.ActOfDelineationMapper;
import kz.kus.sa.tech.condition.dao.repository.ActOfDelineationRenewalExecutionRepository;
import kz.kus.sa.tech.condition.dao.repository.ActOfDelineationRepository;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationCreateDto;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationDto;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationSearchDto;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationUpdateDto;
import kz.kus.sa.tech.condition.exception.ErrorCode;
import kz.kus.sa.tech.condition.exception.NotFoundException;
import kz.kus.sa.tech.condition.service.act.ActOfDelineationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static kz.kus.sa.tech.condition.dao.specification.ActOfDelineationSpecification.*;
import static kz.kus.sa.tech.condition.dao.specification.ActOfDelineationSpecification.byIdentifierList;
import static kz.kus.sa.tech.condition.util.CommonUtils.isNullOrEmpty;
import static org.springframework.data.jpa.domain.Specification.where;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ActOfDelineationServiceImpl implements ActOfDelineationService {

    private final ProviderApiService providerApiService;
    private final ActOfDelineationMapper actOfDelineationMapper;
    private final ActOfDelineationRepository actOfDelineationRepository;
    private final RegistryGenerateNumberApiService registryGenerateNumberApiService;
    private final ActOfDelineationRenewalExecutionRepository actOfDelineationRenewalExecutionRepository;

    @Override
    public Page<ActOfDelineationDto> getAll(ActOfDelineationSearchDto search, Pageable pageable) {
        Specification<ActOfDelineationEntity> specification = where(isNotDeleted()
                .and(byIdList(search.getIdList()))
                .and(byRegistrationNumber(search.getRegistrationNumber()))
                .and(byIdentifierList(search.getConsumerIinBinList())));
        return actOfDelineationRepository.findAll(specification, pageable).map(actOfDelineationMapper::toDto);
    }

    @Override
    public ActOfDelineationDto create(ActOfDelineationCreateDto dto) {
        var entity = actOfDelineationMapper.toEntity(dto);

        this.setRegistrationNumber(entity, dto.getProviderId());

        var execution = this.findExecutionById(dto.getActOfDelineationRenewalExecutionId());
        execution.setActId(entity.getId());
        execution.setActRegistrationNumber(entity.getRegistrationNumber());
        execution.setActDate(entity.getPreparationDatetime().toLocalDate());
        this.baseSave(execution);

        entity.setActOfDelineationRenewalExecution(execution);

        entity = baseSave(entity);
        log.info("ACT OF DELINEATION [CREATED]: id = [{}]", entity.getId());
        return actOfDelineationMapper.toDto(entity);
    }

    @Override
    public ActOfDelineationDto update(UUID id, ActOfDelineationUpdateDto dto) {
        var entity = this.findById(id);
        entity = actOfDelineationMapper.toEntity(entity, dto);

        var execution = this.findExecutionById(dto.getActOfDelineationRenewalExecutionId());
        entity.setActOfDelineationRenewalExecution(execution);

        entity = baseSave(entity);
        log.info("ACT OF DELINEATION [UPDATED]: id = [{}]", entity.getId());
        return actOfDelineationMapper.toDto(entity);
    }

    @Override
    public ActOfDelineationDto getById(UUID id) {
        return actOfDelineationMapper.toDto(this.findById(id));
    }

    private void setRegistrationNumber(ActOfDelineationEntity entity, UUID providerId) {
        var providerDto = providerApiService.getProviderDto(providerId);
        var activityTypeCodes = isNullOrEmpty(providerDto.getActivityTypeCodes())
                ? providerDto.getActivityTypes().stream()
                  .map(EntryDto::getCode)
                  .distinct()
                  .sorted()
                  .collect(Collectors.joining())
                : providerDto.getActivityTypeCodes().stream()
                  .map(code -> String.valueOf(ActivityType.findByCode(code)))
                  .distinct()
                  .sorted()
                  .collect(Collectors.joining());

        var nextRegistrationNumber = actOfDelineationRepository.getMaxInternalRegistrationNumberByProviderIdAndYear(
                providerId, OffsetDateTime.now().getYear()) + 1;

        entity.setPreparationDatetime(OffsetDateTime.now());
        entity.setInternalRegistrationNumber(nextRegistrationNumber);
        entity.setRegistrationNumber(
                registryGenerateNumberApiService.generateActNumber(
                        providerDto.getCode(),
                        RegistrationNumberActType.AR,
                        activityTypeCodes,
                        nextRegistrationNumber)
        );
    }

    @Override
    public ActOfDelineationEntity findByRenewalExecutionId(UUID renewalExecutionId) {
        return actOfDelineationRepository.findByActOfDelineationRenewalExecutionId(renewalExecutionId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    private ActOfDelineationEntity findById(UUID id) {
        return actOfDelineationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    private ActOfDelineationRenewalExecutionEntity findExecutionById(UUID id) {
        return actOfDelineationRenewalExecutionRepository.findByIdAndDeletedDatetimeIsNull(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    private ActOfDelineationEntity baseSave(ActOfDelineationEntity entity) {
        OffsetDateTime now = OffsetDateTime.now();
        if (isNull(entity.getCreatedDatetime())) {
            entity.setCreatedDatetime(now);
        }
        entity.setLastModifiedDatetime(now);
        return actOfDelineationRepository.save(entity);
    }

    private void baseSave(ActOfDelineationRenewalExecutionEntity entity) {
        OffsetDateTime now = OffsetDateTime.now();
        if (isNull(entity.getCreatedDatetime())) {
            entity.setCreatedDatetime(now);
        }
        entity.setLastModifiedDatetime(now);
        actOfDelineationRenewalExecutionRepository.save(entity);
    }
}
