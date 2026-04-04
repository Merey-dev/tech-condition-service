package condition.dao.mapper;

import kz.kus.sa.integ.kzharyq.dto.internal.tc.completed.TechConditionCompletedRequestDto;
import kz.kus.sa.integ.kzharyq.dto.internal.tc.registered.TechConditionRegisteredRequestDto;
import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionProjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface KzharyqTechConditionServiceMapper {

    @Mapping(target = "registrationNumber", source = "statementRegistrationNumber")
    @Mapping(target = "consumerName", source = "consumerFullNameRu")
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "subConsumers", ignore = true)
    @Mapping(target = "reliabilityCategories", ignore = true)
    TechConditionRegisteredRequestDto toDto(TechConditionEntity in);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "registrationNumber", source = "techCondition.statementRegistrationNumber")
    @Mapping(target = "techConditionRegistrationNumber", source = "registrationNumber")
    void map(@MappingTarget TechConditionCompletedRequestDto.TechConditionCompletedRequestDtoBuilder builder,
             TechConditionProjectEntity project);
}
