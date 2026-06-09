package kz.kus.sa.tech.condition.dao.mapper;

import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationEntity;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationCreateDto;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationDto;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {
        ElectrifiedInstallationMapper.class,
        ActOfDelineationAbdAddressMapper.class,
})
public interface ActOfDelineationMapper {

    @Mapping(target = "createdDatetime", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "lastModifiedDatetime", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "decision", ignore = true)
    @Mapping(target = "actOfDelineationRenewal", ignore = true)
    @Mapping(target = "objectAbdAddresses", ignore = true)
    @Mapping(target = "electrifiedInstallations", ignore = true)
    ActOfDelineationEntity toEntity(ActOfDelineationCreateDto dto);

    @Mapping(target = "lastModifiedDatetime", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "decision", ignore = true)
    @Mapping(target = "actOfDelineationRenewal", ignore = true)
    @Mapping(target = "objectAbdAddresses", ignore = true)
    @Mapping(target = "electrifiedInstallations", ignore = true)
    ActOfDelineationEntity toEntity(@MappingTarget ActOfDelineationEntity entity, ActOfDelineationUpdateDto dto);

    @Mapping(target = "statementId", source = "entity.actOfDelineationRenewal.statementId")
    @Mapping(target = "actOfDelineationRenewalId", source = "entity.actOfDelineationRenewal.id")
    ActOfDelineationDto toDto(ActOfDelineationEntity entity);
}
