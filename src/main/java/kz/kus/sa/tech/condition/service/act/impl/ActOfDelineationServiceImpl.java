package kz.kus.sa.tech.condition.service.act.impl;

import kz.kus.sa.auth.api.provider.ProviderApiService;
import kz.kus.sa.auth.api.provider.dto.EntryDto;
import kz.kus.sa.auth.api.provider.enums.ActivityType;
import kz.kus.sa.registry.api.RegistryGenerateNumberApiService;
import kz.kus.sa.registry.enums.RegistrationNumberActType;
import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationEntity;
import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationRenewalAbdAddressDecisionEntity;
import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationRenewalEntity;
import kz.kus.sa.tech.condition.dao.mapper.ActOfDelineationAbdAddressMapper;
import kz.kus.sa.tech.condition.dao.mapper.ActOfDelineationMapper;
import kz.kus.sa.tech.condition.dao.repository.ActOfDelineationRenewalAbdAddressDecisionRepository;
import kz.kus.sa.tech.condition.dao.repository.ActOfDelineationRenewalRepository;
import kz.kus.sa.tech.condition.dao.repository.ActOfDelineationRepository;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationCreateDto;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationDto;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationSearchDto;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationUpdateDto;
import kz.kus.sa.tech.condition.exception.ErrorCode;
import kz.kus.sa.tech.condition.exception.NotFoundException;
import kz.kus.sa.tech.condition.service.act.ActOfDelineationService;
import kz.kus.sa.tech.condition.service.address.AbdAddressService;
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
import static kz.kus.sa.tech.condition.util.CommonUtils.isNullOrEmpty;
import static org.springframework.data.jpa.domain.Specification.where;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ActOfDelineationServiceImpl implements ActOfDelineationService {

    private final AbdAddressService abdAddressService;
    private final ProviderApiService providerApiService;
    private final ActOfDelineationMapper actOfDelineationMapper;
    private final ActOfDelineationAbdAddressMapper abdAddressMapper;
    private final ActOfDelineationRepository actOfDelineationRepository;
    private final RegistryGenerateNumberApiService registryGenerateNumberApiService;
    private final ActOfDelineationRenewalRepository actOfDelineationRenewalRepository;
    private final ActOfDelineationRenewalAbdAddressDecisionRepository decisionRepository;

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

        var renewal = this.findRenewalByStatementId(dto.getStatementId());
        entity.setActOfDelineationRenewal(renewal);

        ActOfDelineationRenewalAbdAddressDecisionEntity decision = resolveDecisionForCreation(dto, renewal.getId());
        if (decision != null) {
            entity.setDecision(decision);
        }

        if (entity.getElectrifiedInstallations() != null) {
            ActOfDelineationEntity finalEntity = entity;
            entity.getElectrifiedInstallations().forEach(ei -> ei.setActOfDelineation(finalEntity));
        }
        entity = baseSave(entity);

        abdAddressService.saveList(null, abdAddressMapper.toEntityList(dto.getObjectAbdAddresses()), entity);

        if (decision != null) {
            decision.setActOfDelineation(entity);
            decisionRepository.save(decision);
        }

        // Сохраняем последние данные акта на заявлении (для совместимости со старыми отчётами)
        renewal.setActId(entity.getId());
        renewal.setActRegistrationNumber(entity.getRegistrationNumber());
        renewal.setActDate(entity.getPreparationDatetime().toLocalDate());
        actOfDelineationRenewalRepository.save(renewal);

        log.info("ACT OF DELINEATION [CREATED]: id = [{}], decisionId = [{}]",
                entity.getId(), decision != null ? decision.getId() : null);
        return actOfDelineationMapper.toDto(entity);
    }

    @Override
    public ActOfDelineationDto update(UUID id, ActOfDelineationUpdateDto dto) {
        var entity = this.findById(id);
        entity = actOfDelineationMapper.toEntity(entity, dto);

        var renewal = this.findRenewalByStatementId(dto.getStatementId());
        entity.setActOfDelineationRenewal(renewal);

        if (entity.getElectrifiedInstallations() != null) {
            ActOfDelineationEntity finalEntity = entity;
            entity.getElectrifiedInstallations().forEach(ei -> ei.setActOfDelineation(finalEntity));
        }

        abdAddressService.saveList(entity.getObjectAbdAddresses(), abdAddressMapper.toEntityList(dto.getObjectAbdAddresses()), entity);

        entity = baseSave(entity);
        log.info("ACT OF DELINEATION [UPDATED]: id = [{}]", entity.getId());
        return actOfDelineationMapper.toDto(entity);
    }

    @Override
    public ActOfDelineationDto getById(UUID id) {
        return actOfDelineationMapper.toDto(this.findById(id));
    }

    @Override
    public ActOfDelineationEntity findByRenewalId(UUID renewalId) {
        return actOfDelineationRepository.findByActOfDelineationRenewalId(renewalId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    private ActOfDelineationRenewalAbdAddressDecisionEntity resolveDecisionForCreation(ActOfDelineationCreateDto dto, UUID renewalId) {
        if (isNullOrEmpty(dto.getObjectAbdAddresses())) {
            return null;
        }
        UUID abdAddressId = dto.getObjectAbdAddresses().get(0).getId();
        if (abdAddressId == null) {
            return null;
        }
        return decisionRepository
                .findByActOfDelineationRenewalIdAndObjectAbdAddressId(renewalId, abdAddressId)
                .orElse(null);
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

    private ActOfDelineationEntity findById(UUID id) {
        return actOfDelineationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    private ActOfDelineationRenewalEntity findRenewalByStatementId(UUID statementId) {
        return actOfDelineationRenewalRepository.findByStatementIdAndDeletedDatetimeIsNull(statementId)
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
}
