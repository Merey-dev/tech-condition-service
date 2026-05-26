package kz.kus.sa.tech.condition.dao.mapper;

import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationRenewalAbdAddressDecisionEntity;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationRenewalAbdAddressDecisionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {
        ExternalUserMapper.class,
        ExternalSubdivisionMapper.class,
        ExternalFileMapper.class,
        ActOfDelineationMapper.class,
})
public interface ActOfDelineationRenewalAbdAddressDecisionMapper {

    @Mapping(target = "abdAddressId", source = "objectAbdAddress.id")
    @Mapping(target = "actOfDelineationRenewalId", source = "actOfDelineationRenewal.id")
    ActOfDelineationRenewalAbdAddressDecisionDto toDto(ActOfDelineationRenewalAbdAddressDecisionEntity entity);

    List<ActOfDelineationRenewalAbdAddressDecisionDto> toDtoList(List<ActOfDelineationRenewalAbdAddressDecisionEntity> entityList);
}
