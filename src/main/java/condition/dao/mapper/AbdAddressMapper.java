package condition.dao.mapper;

import kz.kus.sa.registry.dto.common.AbdAddressDto;
import kz.kus.sa.tech.condition.dao.entity.AbdAddressEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AbdAddressMapper {
    @Mapping(target = "createdDatetime", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "lastModifiedDatetime", expression = "java(java.time.OffsetDateTime.now())")
    AbdAddressEntity toEntity(AbdAddressDto dto);

    List<AbdAddressEntity> toEntityList(List<AbdAddressDto> dtoList);

    @Mapping(target = "lastModifiedDatetime", expression = "java(java.time.OffsetDateTime.now())")
    AbdAddressEntity toEntity(@MappingTarget AbdAddressEntity entity, AbdAddressDto dto);

    AbdAddressDto toDto(AbdAddressEntity entity);

    List<AbdAddressDto> toDtoList(List<AbdAddressEntity> entityList);
}
