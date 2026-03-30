package kz.kus.sa.tech.condition.dao.mapper;

import kz.kus.sa.tech.condition.dao.entity.ElectrifiedInstallationEntity;
import kz.kus.sa.tech.condition.dto.act.ElectrifiedInstallationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ElectrifiedInstallationMapper {
    @Mapping(target = "createdDatetime", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "lastModifiedDatetime", expression = "java(java.time.OffsetDateTime.now())")
    ElectrifiedInstallationEntity toEntity(ElectrifiedInstallationDto dto);

    List<ElectrifiedInstallationEntity> toEntityList(List<ElectrifiedInstallationDto> dtoList);

    @Mapping(target = "lastModifiedDatetime", expression = "java(java.time.OffsetDateTime.now())")
    ElectrifiedInstallationEntity toEntity(@MappingTarget ElectrifiedInstallationEntity entity, ElectrifiedInstallationDto dto);

    ElectrifiedInstallationDto toDto(ElectrifiedInstallationEntity entity);

    List<ElectrifiedInstallationDto> toDtoList(List<ElectrifiedInstallationEntity> entityList);
}
