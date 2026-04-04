package condition.dao.mapper;

import kz.kus.sa.registry.dto.common.AbdAddressDto;
import kz.kus.sa.registry.dto.renewal.ActOfDelineationRenewalDto;
import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationRenewalEntity;
import kz.kus.sa.tech.condition.dao.mapper.ExternalSubdivisionMapper;
import kz.kus.sa.tech.condition.service.address.AbdAddressService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", uses = {
        kz.kus.sa.tech.condition.dao.mapper.AbdAddressMapper.class,
        kz.kus.sa.tech.condition.dao.mapper.ExternalUserMapper.class,
        kz.kus.sa.tech.condition.dao.mapper.ExternalFileMapper.class,
        ExternalSubdivisionMapper.class,
},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class ActOfDelineationRenewalMapper {

    @Autowired
    private kz.kus.sa.tech.condition.dao.mapper.AbdAddressMapper abdAddressMapper;
    @Autowired
    private AbdAddressService abdAddressService;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "statementId", source = "dto.id")
    @Mapping(target = "statementRegistrationNumber", source = "dto.registrationNumber")
    @Mapping(target = "statementRegistrationDatetime", source = "dto.registrationDatetime")
    public abstract ActOfDelineationRenewalEntity toEntity(ActOfDelineationRenewalDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "statementId", source = "dto.id")
    @Mapping(target = "statementRegistrationNumber", source = "dto.registrationNumber")
    @Mapping(target = "statementRegistrationDatetime", source = "dto.registrationDatetime")
    public abstract ActOfDelineationRenewalEntity toEntity(@MappingTarget ActOfDelineationRenewalEntity entity, ActOfDelineationRenewalDto dto);

    @Mapping(target = "id", source = "entity.statementId")
    @Mapping(target = "registrationNumber", source = "entity.statementRegistrationNumber")
    @Mapping(target = "registrationDatetime", source = "entity.statementRegistrationDatetime")
    @Mapping(target = "statementType", constant = "ACT_OF_DELINEATION_RENEWAL")
    @Mapping(target = "objectAbdAddresses", source = "id", qualifiedByName = "objectAbdAddresses")
    public abstract ActOfDelineationRenewalDto toStatementDto(ActOfDelineationRenewalEntity entity);

    @Mapping(target = "id", source = "entity.statementId")
    @Mapping(target = "registrationNumber", source = "entity.statementRegistrationNumber")
    @Mapping(target = "registrationDatetime", source = "entity.statementRegistrationDatetime")
    @Mapping(target = "statementType", constant = "ACT_OF_DELINEATION_RENEWAL")
    @Mapping(target = "objectAbdAddresses", source = "id", qualifiedByName = "objectAbdAddresses")
    public abstract ActOfDelineationRenewalDto toStatementDto(@MappingTarget ActOfDelineationRenewalDto dto, ActOfDelineationRenewalEntity entity);


    @Named("objectAbdAddresses")
    protected List<AbdAddressDto> objectAbdAddresses(UUID actOfDelineationRenewalId) {
        return abdAddressMapper.toDtoList(abdAddressService.getByActOfDelineationRenewalId(actOfDelineationRenewalId));
    }
}
