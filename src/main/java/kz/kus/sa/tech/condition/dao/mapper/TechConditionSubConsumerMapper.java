package kz.kus.sa.tech.condition.dao.mapper;

import kz.kus.sa.registry.dto.tc.v1.TechConditionSubConsumerCreateDto;
import kz.kus.sa.tech.condition.dao.entity.TechConditionSubConsumerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TechConditionSubConsumerMapper {
    @Mapping(target = "createdDatetime", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "lastModifiedDatetime", expression = "java(java.time.OffsetDateTime.now())")
    TechConditionSubConsumerEntity toEntity(TechConditionSubConsumerCreateDto dto);

    List<TechConditionSubConsumerEntity> toEntityList(List<TechConditionSubConsumerCreateDto> dtoList);

    @Mapping(target = "lastModifiedDatetime", expression = "java(java.time.OffsetDateTime.now())")
    TechConditionSubConsumerEntity toEntity(@MappingTarget TechConditionSubConsumerEntity entity, TechConditionSubConsumerCreateDto dto);

    TechConditionSubConsumerCreateDto toDto(TechConditionSubConsumerEntity entity);

    List<TechConditionSubConsumerCreateDto> toDtoList(List<TechConditionSubConsumerEntity> entityList);
}
