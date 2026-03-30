package kz.kus.sa.tech.condition.dao.mapper;

import kz.kus.sa.registry.dto.common.IntersectionDto;
import kz.kus.sa.tech.condition.dao.entity.IntersectionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;

@Mapper(componentModel = "spring")
public interface IntersectionMapper {
    IntersectionEntity toEntity(IntersectionDto dto);

    List<IntersectionEntity> toEntityList(List<IntersectionDto> dtoList);

    IntersectionEntity toEntity(@MappingTarget IntersectionEntity entity, IntersectionDto dto);

    @Named("updateIntersections")
    default List<IntersectionEntity> toEntityList(@MappingTarget List<IntersectionEntity> entityList, List<IntersectionDto> dtoList) {
        if (isEmpty(entityList) || isEmpty(dtoList)) {
            return entityList;
        }

        Map<UUID, IntersectionDto> dtoMap = dtoList.stream()
                .filter(dto -> dto.getId() != null)
                .collect(Collectors.toMap(IntersectionDto::getId, dto -> dto));

        for (IntersectionEntity entity : entityList) {
            IntersectionDto dto = dtoMap.get(entity.getId());
            if (dto != null) {
                toEntity(entity, dto);
            }
        }
        return entityList;
    }

    IntersectionDto toDto(IntersectionEntity entity);

    List<IntersectionDto> toDtoList(List<IntersectionEntity> entityList);
}
