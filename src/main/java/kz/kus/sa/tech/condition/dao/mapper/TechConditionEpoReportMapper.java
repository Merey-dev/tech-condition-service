package kz.kus.sa.tech.condition.dao.mapper;

import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionEntity;
import kz.kus.sa.tech.condition.dto.report.epo.TechConditionEpoApplicationReportDto;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Slf4j
@Mapper(componentModel = "spring", uses = {
        ExternalFileMapper.class,
        ExternalUserMapper.class,
        ExternalSubdivisionMapper.class,
})
public abstract class TechConditionEpoReportMapper {

    @Mapping(target = "consumerFullNameRu", source = "techCondition.consumerFullNameRu")
    @Mapping(target = "consumerFullNameKk", source = "techCondition.consumerFullNameKk")
    @Mapping(target = "consumerIinBin", source = "techCondition.consumerIinBin")
    @Mapping(target = "applicationDatetime", expression = "java(kz.kus.sa.tech.condition.util.CommonUtils.formattedDate(entity.getTechCondition().getApplicationDatetime()))")
    @Mapping(target = "registrationNumber", source = "techCondition.statementRegistrationNumber")
    @Mapping(target = "requiredPower", source = "techCondition.requiredPower")
    public abstract TechConditionEpoApplicationReportDto toApplicationReportDto(TechConditionExecutionEntity entity);
}
