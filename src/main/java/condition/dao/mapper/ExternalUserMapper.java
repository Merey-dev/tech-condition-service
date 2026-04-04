package condition.dao.mapper;

import kz.kus.sa.auth.api.currentuser.dto.CurrentUserResponse;
import kz.kus.sa.auth.api.user.dto.UserDto;
import kz.kus.sa.tech.condition.dao.entity.embedded.ExternalUserEmbedded;
import kz.kus.sa.registry.dto.common.ExternalUserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExternalUserMapper {
    @Mapping(target = "providerId", source = "dto.organizationId")
    @Mapping(target = "providerName", source = "dto.organizationName")
    @Mapping(target = "division", source = "dto.subDivisionName")
    @Mapping(target = "position", source = "dto.positionName")
    ExternalUserEmbedded toEntity(UserDto dto);

    ExternalUserEmbedded toEntity(ExternalUserDto externalUserDto);

    ExternalUserDto toDto(ExternalUserEmbedded externalUserEmbedded);

    @Mapping(target = "providerId", source = "organizationId")
    @Mapping(target = "providerName", source = "organizationName")
    @Mapping(target = "division", source = "subDivisionName")
    @Mapping(target = "position", source = "positionName")
    ExternalUserEmbedded fromCurrentUserResponse(CurrentUserResponse currentUser);
}
