package kz.kus.sa.tech.condition.dao.mapper;

import kz.kus.sa.registry.dto.tc.v1.TechConditionContractualCapacityOfTransformerCreateDto;
import kz.kus.sa.tech.condition.dao.entity.TechConditionContractualCapacityOfTransformerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TechConditionContractualCapacityOfTransformerMapper {
    @Mapping(target = "createdDatetime", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "lastModifiedDatetime", expression = "java(java.time.OffsetDateTime.now())")
    TechConditionContractualCapacityOfTransformerEntity toEntity(TechConditionContractualCapacityOfTransformerCreateDto dto);

    List<TechConditionContractualCapacityOfTransformerEntity> toEntityList(List<TechConditionContractualCapacityOfTransformerCreateDto> dtoList);

    @Mapping(target = "lastModifiedDatetime", expression = "java(java.time.OffsetDateTime.now())")
    TechConditionContractualCapacityOfTransformerEntity toEntity(@MappingTarget TechConditionContractualCapacityOfTransformerEntity entity, TechConditionContractualCapacityOfTransformerCreateDto dto);

    TechConditionContractualCapacityOfTransformerCreateDto toDto(TechConditionContractualCapacityOfTransformerEntity entity);

    List<TechConditionContractualCapacityOfTransformerCreateDto> toDtoList(List<TechConditionContractualCapacityOfTransformerEntity> entityList);
}
