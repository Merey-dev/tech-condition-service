package condition.dao.mapper;

import kz.kus.sa.registry.dto.tc.v1.TechConditionReliabilityCategoryCreateDto;
import kz.kus.sa.tech.condition.dao.entity.TechConditionReliabilityCategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TechConditionReliabilityCategoryMapper {
    @Mapping(target = "createdDatetime", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "lastModifiedDatetime", expression = "java(java.time.OffsetDateTime.now())")
    TechConditionReliabilityCategoryEntity toEntity(TechConditionReliabilityCategoryCreateDto dto);

    List<TechConditionReliabilityCategoryEntity> toEntityList(List<TechConditionReliabilityCategoryCreateDto> dtoList);

    @Mapping(target = "lastModifiedDatetime", expression = "java(java.time.OffsetDateTime.now())")
    TechConditionReliabilityCategoryEntity toEntity(@MappingTarget TechConditionReliabilityCategoryEntity entity, TechConditionReliabilityCategoryCreateDto dto);

    TechConditionReliabilityCategoryCreateDto toDto(TechConditionReliabilityCategoryEntity entity);

    List<TechConditionReliabilityCategoryCreateDto> toDtoList(List<TechConditionReliabilityCategoryEntity> entityList);
}
