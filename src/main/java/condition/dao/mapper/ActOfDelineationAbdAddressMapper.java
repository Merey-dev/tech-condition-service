package condition.dao.mapper;

import kz.kus.sa.tech.condition.dao.entity.AbdAddressEntity;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationAbdAddressDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ActOfDelineationAbdAddressMapper {
    @Mapping(target = "createdDatetime", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "lastModifiedDatetime", expression = "java(java.time.OffsetDateTime.now())")
    AbdAddressEntity toEntity(ActOfDelineationAbdAddressDto dto);

    List<AbdAddressEntity> toEntityList(List<ActOfDelineationAbdAddressDto> dtoList);

    @Mapping(target = "lastModifiedDatetime", expression = "java(java.time.OffsetDateTime.now())")
    AbdAddressEntity toEntity(@MappingTarget AbdAddressEntity entity, ActOfDelineationAbdAddressDto dto);

    ActOfDelineationAbdAddressDto toDto(AbdAddressEntity entity);

    List<ActOfDelineationAbdAddressDto> toDtoList(List<AbdAddressEntity> entityList);
}
