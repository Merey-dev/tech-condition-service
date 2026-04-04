package condition.dao.mapper;

import kz.kus.sa.registry.dto.tc.v1.TechConditionPlannedEquipmentCreateDto;
import kz.kus.sa.tech.condition.dao.entity.TechConditionPlannedEquipmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TechConditionPlannedEquipmentMapper {
    @Mapping(target = "createdDatetime", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "lastModifiedDatetime", expression = "java(java.time.OffsetDateTime.now())")
    TechConditionPlannedEquipmentEntity toEntity(TechConditionPlannedEquipmentCreateDto dto);

    List<TechConditionPlannedEquipmentEntity> toEntityList(List<TechConditionPlannedEquipmentCreateDto> dtoList);

    @Mapping(target = "lastModifiedDatetime", expression = "java(java.time.OffsetDateTime.now())")
    TechConditionPlannedEquipmentEntity toEntity(@MappingTarget TechConditionPlannedEquipmentEntity entity, TechConditionPlannedEquipmentCreateDto dto);

    TechConditionPlannedEquipmentCreateDto toDto(TechConditionPlannedEquipmentEntity entity);

    List<TechConditionPlannedEquipmentCreateDto> toDtoList(List<TechConditionPlannedEquipmentEntity> entityList);
}
