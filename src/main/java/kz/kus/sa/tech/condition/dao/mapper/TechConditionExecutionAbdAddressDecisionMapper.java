package kz.kus.sa.tech.condition.dao.mapper;
import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionAbdAddressDecisionEntity;
import kz.kus.sa.tech.condition.dto.execution.TechConditionExecutionAbdAddressDecisionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {
        ExternalUserMapper.class,
        ExternalSubdivisionMapper.class,
        TechConditionProjectMapper.class,
})
public interface TechConditionExecutionAbdAddressDecisionMapper {

    @Mapping(target = "abdAddressId", source = "objectAbdAddress.id")
    TechConditionExecutionAbdAddressDecisionDto toDto(TechConditionExecutionAbdAddressDecisionEntity entity);

    List<TechConditionExecutionAbdAddressDecisionDto> toDtoList(List<TechConditionExecutionAbdAddressDecisionEntity> entityList);
}
