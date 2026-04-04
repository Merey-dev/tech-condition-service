package condition.service.address.impl;

import kz.kus.sa.tech.condition.dao.entity.*;
import kz.kus.sa.tech.condition.dao.repository.AbdAddressRepository;
import kz.kus.sa.tech.condition.exception.BadRequestException;
import kz.kus.sa.tech.condition.service.address.AbdAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class AbdAddressServiceImpl implements AbdAddressService {

    private final AbdAddressRepository abdAddressRepository;

    @Override
    public void saveList(List<AbdAddressEntity> oldList, List<AbdAddressEntity> newList, Object object) {
        /*if (nonNull(object) && !isEmpty(newList)) {
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
            else if (object instanceof ActOfDelineationRenewalEntity)
                this.save(newList.stream()
                        .peek(e -> e.setActOfDelineationRenewal((ActOfDelineationRenewalEntity) object))
                        .collect(Collectors.toList()));
            else if (object instanceof ActOfDelineationRenewalExecutionEntity)
                this.save(newList.stream()
                        .peek(e -> e.setActOfDelineationRenewalExecution((ActOfDelineationRenewalExecutionEntity) object))
                        .collect(Collectors.toList()));
            else if (object instanceof ActOfDelineationEntity)
                this.save(newList.stream()
                        .peek(e -> e.setActOfDelineation((ActOfDelineationEntity) object))
                        .collect(Collectors.toList()));
            else
                throw new BadRequestException("Object type not supported");
            if (nonNull(oldList) && !oldList.isEmpty())
                this.delete(oldList);
        }*/

        List<AbdAddressEntity> currentList = isNull(oldList) ? new ArrayList<>() : oldList;
        List<AbdAddressEntity> incomingList = isNull(newList) ? new ArrayList<>() : newList;

        Map<UUID, AbdAddressEntity> currentById = currentList.stream()
                .filter(e -> e.getId() != null)
                .collect(toMap(AbdAddressEntity::getId, Function.identity()));

        List<AbdAddressEntity> toSave = new ArrayList<>();
        Set<UUID> incomingIds = new HashSet<>();

        for (AbdAddressEntity incoming : incomingList) {
            AbdAddressEntity target = null;

            if (incoming.getId() != null) {
                target = currentById.get(incoming.getId());
                incomingIds.add(incoming.getId());
            }

            if (target == null) {
                target = new AbdAddressEntity();
            }

            this.copyFields(incoming, target);
            this.bindOwner(target, object);

            toSave.add(target);
        }

        List<AbdAddressEntity> toDelete = currentList.stream()
                .filter(e -> e.getId() != null)
                .filter(e -> !incomingIds.contains(e.getId()))
                .collect(toList());

        if (!isEmpty(toSave)) {
            this.save(toSave);
        }

        if (!isEmpty(toDelete)) {
            this.delete(toDelete);
        }
    }

    @Override
    public List<AbdAddressEntity> getByTechConditionId(UUID techConditionId) {
        return abdAddressRepository.findAllByTechConditionId(techConditionId);
    }

    @Override
    public List<AbdAddressEntity> getByTechConditionExecutionId(UUID techConditionExecutionId) {
        return abdAddressRepository.findAllByTechConditionExecutionId(techConditionExecutionId);
    }

    @Override
    public List<AbdAddressEntity> getByTechConditionProjectId(UUID techConditionProjectId) {
        return abdAddressRepository.findAllByTechConditionProjectId(techConditionProjectId);
    }

    @Override
    public List<AbdAddressEntity> getByActOfDelineationRenewalId(UUID actOfDelineationRenewalId) {
        return abdAddressRepository.findAllByActOfDelineationRenewalId(actOfDelineationRenewalId);
    }

    @Override
    public List<AbdAddressEntity> getByActOfDelineationRenewalExecutionId(UUID actOfDelineationRenewalExecutionId) {
        return abdAddressRepository.findAllByActOfDelineationRenewalExecutionId(actOfDelineationRenewalExecutionId);
    }

    @Override
    public List<AbdAddressEntity> getByActOfDelineationId(UUID actOfDelineationId) {
        return abdAddressRepository.findAllByActOfDelineationId(actOfDelineationId);
    }

    private void delete(List<AbdAddressEntity> list) {
        abdAddressRepository.deleteAll(list);
        log.info("Deleted {} AbdAddresses", list.size());
    }

    private void save(List<AbdAddressEntity> list) {
        abdAddressRepository.saveAll(list);
        log.info("Saved {} AbdAddresses", list.size());
    }

    @Override
    public void deleteDatetime(List<AbdAddressEntity> list) {
        list.forEach(item -> item.setDeletedDatetime(OffsetDateTime.now()));
        this.save(list);
        log.info("DeletedDateTime {} AbdAddresses", list.size());
    }

    private void bindOwner(AbdAddressEntity entity, Object object) {
        entity.setTechCondition(null);
        entity.setTechConditionExecution(null);
        entity.setTechConditionProject(null);
        entity.setActOfDelineationRenewal(null);
        entity.setActOfDelineationRenewalExecution(null);
        entity.setActOfDelineation(null);

        if (object instanceof TechConditionEntity) {
            entity.setTechCondition((TechConditionEntity) object);
        } else if (object instanceof TechConditionExecutionEntity) {
            entity.setTechConditionExecution((TechConditionExecutionEntity) object);
        } else if (object instanceof TechConditionProjectEntity) {
            entity.setTechConditionProject((TechConditionProjectEntity) object);
        } else if (object instanceof ActOfDelineationRenewalEntity) {
            entity.setActOfDelineationRenewal((ActOfDelineationRenewalEntity) object);
        } else if (object instanceof ActOfDelineationRenewalExecutionEntity) {
            entity.setActOfDelineationRenewalExecution((ActOfDelineationRenewalExecutionEntity) object);
        } else if (object instanceof ActOfDelineationEntity) {
            entity.setActOfDelineation((ActOfDelineationEntity) object);
        } else {
            throw new BadRequestException("Object type not supported");
        }
    }

    private void copyFields(AbdAddressEntity source, AbdAddressEntity target) {
        target.setOwner(source.getOwner());
        target.setObjectTypeCode(source.getObjectTypeCode());
        target.setCadastralNumber(source.getCadastralNumber());
        target.setEndUseKk(source.getEndUseKk());
        target.setEndUseRu(source.getEndUseRu());
        target.setDocumentKk(source.getDocumentKk());
        target.setDocumentRu(source.getDocumentRu());
        target.setStoreys(source.getStoreys());
        target.setTotalArea(source.getTotalArea());
        target.setLocationKk(source.getLocationKk());
        target.setLocationRu(source.getLocationRu());
        target.setArRcaCode(source.getArRcaCode());
        target.setMeterDeviceAvailability(source.getMeterDeviceAvailability());
        target.setPropertyTypeCode(source.getPropertyTypeCode());
        target.setLivingArea(source.getLivingArea());
        target.setWithinCondominium(source.getWithinCondominium());
        target.setIsCondominium(source.getIsCondominium());
        target.setManuallyAdded(source.getManuallyAdded());
        target.setObjectTypeIdKk(source.getObjectTypeIdKk());
        target.setObjectTypeIdRu(source.getObjectTypeIdRu());
        target.setConnectionPoints(source.getConnectionPoints());
        target.setPower(source.getPower());
        target.setBorderDemarcationSchemeFile(source.getBorderDemarcationSchemeFile());
        target.setRequiredPower(source.getRequiredPower());
        target.setVoltageLevelCode(source.getVoltageLevelCode());
    }
}
