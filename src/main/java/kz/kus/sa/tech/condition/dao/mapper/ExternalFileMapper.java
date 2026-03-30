package kz.kus.sa.tech.condition.dao.mapper;

import kz.kus.sa.registry.dto.common.FileCreateDto;
import kz.kus.sa.tech.condition.dao.entity.embedded.ExternalFileEmbedded;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExternalFileMapper {
    ExternalFileEmbedded toEntity(FileCreateDto fileCreateDto);

    FileCreateDto toDto(ExternalFileEmbedded externalFileEmbedded);
}
