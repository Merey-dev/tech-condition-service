package kz.kus.sa.tech.condition.dao.mapper;

import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionEntity;
import kz.kus.sa.tech.condition.dto.execution.TechConditionExecutionDto;
import kz.kus.sa.tech.condition.dto.execution.TechConditionExecutionStatementDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = {
        AbdAddressMapper.class,
        IntersectionMapper.class,
        ExternalUserMapper.class,
        ExternalSubdivisionMapper.class,
        TechConditionProjectMapper.class,
})
public interface TechConditionExecutionMapper {

    TechConditionExecutionEntity toEntity(TechConditionExecutionDto dto);

    @Mapping(target = "statement", source = "entity", qualifiedByName = "statement")
    @Mapping(target = "techConditionId", source = "entity.techCondition.id")
    TechConditionExecutionDto toDto(TechConditionExecutionEntity entity);

    List<TechConditionExecutionDto> toDtoList(List<TechConditionExecutionEntity> entityList);

    @Named("statement")
    default TechConditionExecutionStatementDto statement(TechConditionExecutionEntity entity) {
        TechConditionExecutionStatementDto statementDto = new TechConditionExecutionStatementDto();

        statementDto.setConsumerType(entity.getTechCondition().getConsumerType());
        statementDto.setConsumerIinBin(entity.getTechCondition().getConsumerIinBin());
        statementDto.setConsumerFullNameRu(entity.getTechCondition().getConsumerFullNameRu());
        statementDto.setConsumerFullNameKk(entity.getTechCondition().getConsumerFullNameKk());
        statementDto.setStatusCode(entity.getTechCondition().getStatusCode());
        statementDto.setStatementId(entity.getTechCondition().getStatementId());
        statementDto.setStatementRegistrationNumber(entity.getTechCondition().getStatementRegistrationNumber());
        statementDto.setHasParallelExecutions(entity.getTechCondition().getHasParallelExecutions());
        statementDto.setStatementRegistrationDatetime(entity.getTechCondition().getStatementRegistrationDatetime());
        statementDto.setDeadlineDatetime(entity.getTechCondition().getDeadlineDatetime());
//        statementDto.setDaysUntilDeadline(daysUntilDeadline(entity.getTechCondition().getDeadlineDatetime()));//todo mapper
        statementDto.setSource(entity.getTechCondition().getSource());
        statementDto.setSourceCode(entity.getTechCondition().getSource().name());
//        if (nonNull(entity.getTechCondition().getExecutor()))
//            statementDto.setExecutor(entity.getTechCondition().getExecutor());//todo mapper
//        statementDto.setInitiator(entity.getInitiator());//todo mapper

        return statementDto;
    }
}
