package condition.dao.mapper;

import kz.kus.sa.tech.condition.dao.entity.TechConditionProjectEntity;
import kz.kus.sa.tech.condition.dao.mapper.ExternalSubdivisionMapper;
import kz.kus.sa.tech.condition.dto.project.TechConditionProjectCreateDto;
import kz.kus.sa.tech.condition.dto.project.TechConditionProjectUpdateDto;
import kz.kus.sa.tech.condition.dto.project.TechConditionProjectDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {
        kz.kus.sa.tech.condition.dao.mapper.AbdAddressMapper.class,
        kz.kus.sa.tech.condition.dao.mapper.IntersectionMapper.class,
        kz.kus.sa.tech.condition.dao.mapper.ExternalFileMapper.class,
        kz.kus.sa.tech.condition.dao.mapper.ExternalUserMapper.class,
        ExternalSubdivisionMapper.class,
})
public interface TechConditionProjectMapper {
    @Mapping(target = "createdDatetime", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "lastModifiedDatetime", expression = "java(java.time.OffsetDateTime.now())")
    TechConditionProjectEntity toEntity(TechConditionProjectCreateDto dto);

    @Mapping(target = "lastModifiedDatetime", expression = "java(java.time.OffsetDateTime.now())")
    TechConditionProjectEntity toEntity(@MappingTarget TechConditionProjectEntity entity, TechConditionProjectUpdateDto dto);

    TechConditionProjectEntity toEntity(TechConditionProjectDto dto);

    List<TechConditionProjectEntity> toEntityList(List<TechConditionProjectDto> dtoList);

    @Mapping(target = "techConditionId", source = "entity.techCondition.id")
//    @Mapping(target = "techConditionExecutionId", source = "entity.techConditionExecution.id")
    TechConditionProjectDto toDto(TechConditionProjectEntity entity);

    List<TechConditionProjectDto> toDtoList(List<TechConditionProjectEntity> entityList);
}
