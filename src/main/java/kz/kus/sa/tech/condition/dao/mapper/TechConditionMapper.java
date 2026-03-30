package kz.kus.sa.tech.condition.dao.mapper;

import kz.kus.sa.registry.dto.common.AbdAddressDto;
import kz.kus.sa.registry.dto.common.IntersectionDto;
import kz.kus.sa.registry.dto.tc.v1.*;
import kz.kus.sa.tech.condition.dto.TechConditionDto;
import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.service.address.AbdAddressService;
import kz.kus.sa.tech.condition.service.address.IntersectionService;
import kz.kus.sa.tech.condition.service.tech.condition.*;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", uses = {
        AbdAddressMapper.class,
        IntersectionMapper.class,
        ExternalUserMapper.class,
        ExternalFileMapper.class,
        ExternalSubdivisionMapper.class,
        TechConditionMaximumLoadMapper.class,
        TechConditionSubConsumerMapper.class,
        TechConditionPlannedEquipmentMapper.class,
        TechConditionReliabilityCategoryMapper.class,
        TechConditionContractualCapacityOfTransformerMapper.class,
},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class TechConditionMapper {

    @Autowired
    private AbdAddressMapper abdAddressMapper;
    @Autowired
    private AbdAddressService abdAddressService;
    @Autowired
    private IntersectionMapper intersectionMapper;
    @Autowired
    private IntersectionService intersectionService;
    @Autowired
    private TechConditionSubConsumerMapper subConsumerMapper;
    @Autowired
    private TechConditionSubConsumerService subConsumerService;
    @Autowired
    private TechConditionMaximumLoadMapper maximumLoadMapper;
    @Autowired
    private TechConditionMaximumLoadService maximumLoadService;
    @Autowired
    private TechConditionPlannedEquipmentMapper plannedEquipmentMapper;
    @Autowired
    private TechConditionPlannedEquipmentService plannedEquipmentService;
    @Autowired
    private TechConditionReliabilityCategoryMapper reliabilityCategoryMapper;
    @Autowired
    private TechConditionReliabilityCategoryService reliabilityCategoryService;
    @Autowired
    private TechConditionContractualCapacityOfTransformerMapper contractualCapacityOfTransformerMapper;
    @Autowired
    private TechConditionContractualCapacityOfTransformerService contractualCapacityOfTransformerService;


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "statementId", source = "dto.id")
    @Mapping(target = "statementRegistrationNumber", source = "dto.registrationNumber")
    @Mapping(target = "statementRegistrationDatetime", source = "dto.registrationDatetime")
    @Mapping(target = "objectAbdAddresses", ignore = true)
    @Mapping(target = "intersections", ignore = true)
    @Mapping(target = "subConsumers", ignore = true)
    @Mapping(target = "maximumLoads", ignore = true)
    @Mapping(target = "plannedEquipments", ignore = true)
    @Mapping(target = "contractualCapacityOfTransformers", ignore = true)
    @Mapping(target = "reliabilityCategories", ignore = true)
    public abstract TechConditionEntity toEntity(TechConditionStatementDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "statementId", source = "dto.id")
    @Mapping(target = "statementRegistrationNumber", source = "dto.registrationNumber")
    @Mapping(target = "statementRegistrationDatetime", source = "dto.registrationDatetime")
    @Mapping(target = "objectAbdAddresses", ignore = true)
    @Mapping(target = "intersections", ignore = true)
    @Mapping(target = "subConsumers", ignore = true)
    @Mapping(target = "maximumLoads", ignore = true)
    @Mapping(target = "plannedEquipments", ignore = true)
    @Mapping(target = "contractualCapacityOfTransformers", ignore = true)
    @Mapping(target = "reliabilityCategories", ignore = true)
    public abstract TechConditionEntity toEntity(@MappingTarget TechConditionEntity entity, TechConditionStatementDto dto);

    @Mapping(target = "id", source = "entity.statementId")
    @Mapping(target = "registrationNumber", source = "entity.statementRegistrationNumber")
    @Mapping(target = "registrationDatetime", source = "entity.statementRegistrationDatetime")
    @Mapping(target = "statementType", constant = "TECH_CONDITION")
    @Mapping(target = "objectAbdAddresses", source = "id", qualifiedByName = "objectAbdAddresses")
    @Mapping(target = "intersections", source = "id", qualifiedByName = "intersections")
    @Mapping(target = "subConsumers", source = "id", qualifiedByName = "subConsumers")
    @Mapping(target = "maximumLoads", source = "id", qualifiedByName = "maximumLoads")
    @Mapping(target = "plannedEquipments", source = "id", qualifiedByName = "plannedEquipments")
    @Mapping(target = "contractualCapacityOfTransformers", source = "id", qualifiedByName = "contractualCapacityOfTransformers")
    @Mapping(target = "reliabilityCategories", source = "id", qualifiedByName = "reliabilityCategories")
    public abstract TechConditionStatementDto toStatementDto(TechConditionEntity entity);

    @Mapping(target = "id", source = "entity.statementId")
    @Mapping(target = "registrationNumber", source = "entity.statementRegistrationNumber")
    @Mapping(target = "registrationDatetime", source = "entity.statementRegistrationDatetime")
    @Mapping(target = "statementType", constant = "TECH_CONDITION")
    @Mapping(target = "objectAbdAddresses", source = "id", qualifiedByName = "objectAbdAddresses")
    @Mapping(target = "intersections", source = "id", qualifiedByName = "intersections")
    @Mapping(target = "subConsumers", source = "id", qualifiedByName = "subConsumers")
    @Mapping(target = "maximumLoads", source = "id", qualifiedByName = "maximumLoads")
    @Mapping(target = "plannedEquipments", source = "id", qualifiedByName = "plannedEquipments")
    @Mapping(target = "contractualCapacityOfTransformers", source = "id", qualifiedByName = "contractualCapacityOfTransformers")
    @Mapping(target = "reliabilityCategories", source = "id", qualifiedByName = "reliabilityCategories")
    public abstract TechConditionStatementDto toStatementDto(@MappingTarget TechConditionStatementDto dto, TechConditionEntity entity);

    @Mapping(target = "registrationNumber", source = "entity.statementRegistrationNumber")
    @Mapping(target = "registrationDatetime", source = "entity.statementRegistrationDatetime")
    @Mapping(target = "statementType", constant = "TECH_CONDITION")
    @Mapping(target = "objectAbdAddresses", source = "id", qualifiedByName = "objectAbdAddresses")
    @Mapping(target = "intersections", source = "id", qualifiedByName = "intersections")
    @Mapping(target = "subConsumers", source = "id", qualifiedByName = "subConsumers")
    @Mapping(target = "maximumLoads", source = "id", qualifiedByName = "maximumLoads")
    @Mapping(target = "plannedEquipments", source = "id", qualifiedByName = "plannedEquipments")
    @Mapping(target = "contractualCapacityOfTransformers", source = "id", qualifiedByName = "contractualCapacityOfTransformers")
    @Mapping(target = "reliabilityCategories", source = "id", qualifiedByName = "reliabilityCategories")
    public abstract TechConditionDto toDto(TechConditionEntity entity);


    @Named("objectAbdAddresses")
    protected List<AbdAddressDto> objectAbdAddresses(UUID techConditionId) {
        return abdAddressMapper.toDtoList(abdAddressService.getByTechConditionId(techConditionId));
    }

    @Named("intersections")
    protected List<IntersectionDto> intersections(UUID techConditionId) {
        return intersectionMapper.toDtoList(intersectionService.getByTechConditionId(techConditionId));
    }

    @Named("subConsumers")
    protected List<TechConditionSubConsumerCreateDto> subConsumers(UUID techConditionId) {
        return subConsumerMapper.toDtoList(subConsumerService.getByTechConditionId(techConditionId));
    }

    @Named("maximumLoads")
    protected List<TechConditionMaximumLoadCreateDto> maximumLoads(UUID techConditionId) {
        return maximumLoadMapper.toDtoList(maximumLoadService.getByTechConditionId(techConditionId));
    }

    @Named("plannedEquipments")
    protected List<TechConditionPlannedEquipmentCreateDto> plannedEquipments(UUID techConditionId) {
        return plannedEquipmentMapper.toDtoList(plannedEquipmentService.getByTechConditionId(techConditionId));
    }

    @Named("contractualCapacityOfTransformers")
    protected List<TechConditionContractualCapacityOfTransformerCreateDto> contractualCapacityOfTransformers(UUID techConditionId) {
        return contractualCapacityOfTransformerMapper.toDtoList(contractualCapacityOfTransformerService.getByTechConditionId(techConditionId));
    }

    @Named("reliabilityCategories")
    protected List<TechConditionReliabilityCategoryCreateDto> reliabilityCategories(UUID techConditionId) {
        return reliabilityCategoryMapper.toDtoList(reliabilityCategoryService.getByTechConditionId(techConditionId));
    }
}
