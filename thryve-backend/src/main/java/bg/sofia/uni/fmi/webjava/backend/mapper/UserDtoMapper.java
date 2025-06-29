package bg.sofia.uni.fmi.webjava.backend.mapper;

import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {

    User mapDtoToUser(UserCreateDto userCreateDto);

    UserResponseDto mapUserToResponseDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(UserUpdateDto userUpdateDto, @MappingTarget User user);

}
