package bg.sofia.uni.fmi.webjava.backend.mapper;

import bg.sofia.uni.fmi.webjava.backend.model.dto.CreateUserDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.UpdateUserDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.response.UserResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {

    User mapDtoToUser(CreateUserDto createUserDto);

    UserResponseDto mapUserToResponseDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(UpdateUserDto updateUserDto, @MappingTarget User user);

}
