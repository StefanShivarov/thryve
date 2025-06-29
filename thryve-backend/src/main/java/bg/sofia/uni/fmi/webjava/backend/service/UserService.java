package bg.sofia.uni.fmi.webjava.backend.service;

import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserResponseDto;
import bg.sofia.uni.fmi.webjava.backend.exception.EntityAlreadyExistsException;
import bg.sofia.uni.fmi.webjava.backend.exception.EntityNotFoundException;
import bg.sofia.uni.fmi.webjava.backend.mapper.UserDtoMapper;
import bg.sofia.uni.fmi.webjava.backend.model.entity.User;
import bg.sofia.uni.fmi.webjava.backend.model.entity.UserRole;
import bg.sofia.uni.fmi.webjava.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class UserService {

    public static final String USER_NOT_FOUND_ERROR_MESSAGE = "User with id %s was not found!";
    public static final String USER_WITH_EMAIL_ALREADY_EXISTS_ERROR_MESSAGE = "User with email %s already exists!";
    public static final String USER_WITH_USERNAME_ALREADY_EXISTS_ERROR_MESSAGE = "User with username %s already exists!";

    private final UserRepository userRepository;
    private final UserDtoMapper userDtoMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
            .map(userDtoMapper::mapUserToResponseDto);
    }

    @Transactional
    public UserResponseDto getUserById(UUID id) {
        return userDtoMapper.mapUserToResponseDto(getUserEntityById(id));
    }

    @Transactional
    public User getUserEntityById(UUID id) {
        return userRepository.findById(id).orElseThrow(
            () -> new EntityNotFoundException(
                format(USER_NOT_FOUND_ERROR_MESSAGE, id)));
    }

    @Transactional
    public UserResponseDto createUser(UserCreateDto userCreateDto) {
        if (userRepository.findByEmail(userCreateDto.getEmail()).isPresent()) {
            throw new EntityAlreadyExistsException(
                    format(USER_WITH_EMAIL_ALREADY_EXISTS_ERROR_MESSAGE, userCreateDto.getEmail())
            );
        }

        if (userRepository.findByUsername(userCreateDto.getUsername()).isPresent()) {
            throw new EntityAlreadyExistsException(
                    format(USER_WITH_USERNAME_ALREADY_EXISTS_ERROR_MESSAGE, userCreateDto.getUsername())
            );
        }

        User userToCreate = userDtoMapper.mapDtoToUser(userCreateDto);
        userToCreate.setPassword(passwordEncoder.encode(userCreateDto.getPassword()));
        userToCreate.setRole(UserRole.STANDARD);
        return userDtoMapper.mapUserToResponseDto(userRepository.save(userToCreate));
    }

    @Transactional
    public UserResponseDto updateUserById(UUID id, UserUpdateDto userUpdateDto) {
        User user = getUserEntityById(id);
        userDtoMapper.updateUserFromDto(userUpdateDto, user);
        return userDtoMapper.mapUserToResponseDto(userRepository.save(user));
    }

    @Transactional
    public UserResponseDto deleteUserById(UUID id) {
        UserResponseDto userResponseDto = getUserById(id);
        userRepository.deleteById(id);
        return userResponseDto;
    }

}
