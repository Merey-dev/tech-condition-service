package kz.kus.sa.tech.condition.dao.mapper;

import kz.kus.sa.auth.api.provider.dto.SubdivisionDto;
import kz.kus.sa.tech.condition.dao.entity.embedded.ExternalSubdivisionEmbedded;
import kz.kus.sa.registry.dto.common.ExternalSubdivisionDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExternalSubdivisionMapper {
    ExternalSubdivisionEmbedded toEntity(SubdivisionDto subdivisionDto);

    ExternalSubdivisionEmbedded toEntity(ExternalSubdivisionDto subdivisionDto);

    ExternalSubdivisionDto toDto(ExternalSubdivisionEmbedded subdivisionEmbedded);
}
