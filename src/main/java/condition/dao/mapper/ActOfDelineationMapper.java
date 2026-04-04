package condition.dao.mapper;

import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationEntity;
import kz.kus.sa.tech.condition.dao.mapper.ActOfDelineationAbdAddressMapper;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationCreateDto;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationDto;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {
        kz.kus.sa.tech.condition.dao.mapper.ElectrifiedInstallationMapper.class,
        ActOfDelineationAbdAddressMapper.class,
})
public interface ActOfDelineationMapper {

    @Mapping(target = "createdDatetime", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "lastModifiedDatetime", expression = "java(java.time.OffsetDateTime.now())")
    ActOfDelineationEntity toEntity(ActOfDelineationCreateDto dto);

    @Mapping(target = "lastModifiedDatetime", expression = "java(java.time.OffsetDateTime.now())")
    ActOfDelineationEntity toEntity(@MappingTarget ActOfDelineationEntity entity, ActOfDelineationUpdateDto dto);

    @Mapping(target = "statementId", source = "entity.actOfDelineationRenewalExecution.actOfDelineationRenewal.statementId")
    @Mapping(target = "actOfDelineationRenewalId", source = "entity.actOfDelineationRenewalExecution.actOfDelineationRenewal.id")
    @Mapping(target = "actOfDelineationRenewalExecutionId", source = "entity.actOfDelineationRenewalExecution.id")
    ActOfDelineationDto toDto(ActOfDelineationEntity entity);
}
