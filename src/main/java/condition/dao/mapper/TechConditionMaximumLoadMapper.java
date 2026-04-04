package condition.dao.mapper;

import kz.kus.sa.registry.dto.tc.v1.TechConditionMaximumLoadCreateDto;
import kz.kus.sa.tech.condition.dao.entity.TechConditionMaximumLoadEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TechConditionMaximumLoadMapper {
    @Mapping(target = "createdDatetime", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "lastModifiedDatetime", expression = "java(java.time.OffsetDateTime.now())")
    TechConditionMaximumLoadEntity toEntity(TechConditionMaximumLoadCreateDto dto);

    List<TechConditionMaximumLoadEntity> toEntityList(List<TechConditionMaximumLoadCreateDto> dtoList);

    @Mapping(target = "lastModifiedDatetime", expression = "java(java.time.OffsetDateTime.now())")
    TechConditionMaximumLoadEntity toEntity(@MappingTarget TechConditionMaximumLoadEntity entity, TechConditionMaximumLoadCreateDto dto);

    TechConditionMaximumLoadCreateDto toDto(TechConditionMaximumLoadEntity entity);

    List<TechConditionMaximumLoadCreateDto> toDtoList(List<TechConditionMaximumLoadEntity> entityList);
}
