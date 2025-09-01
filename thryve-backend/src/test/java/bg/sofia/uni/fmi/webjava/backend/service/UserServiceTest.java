package bg.sofia.uni.fmi.webjava.backend.service;

import bg.sofia.uni.fmi.webjava.backend.exception.EntityAlreadyExistsException;
import bg.sofia.uni.fmi.webjava.backend.exception.EntityNotFoundException;
import bg.sofia.uni.fmi.webjava.backend.mapper.UserDtoMapper;
import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.User;
import bg.sofia.uni.fmi.webjava.backend.repository.UserRepository;
import bg.sofia.uni.fmi.webjava.backend.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static bg.sofia.uni.fmi.webjava.backend.service.UserService.USER_NOT_FOUND_ERROR_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.service.UserService.USER_WITH_EMAIL_ALREADY_EXISTS_ERROR_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.service.UserService.USER_WITH_USERNAME_ALREADY_EXISTS_ERROR_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createAdminTestUser;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createAdminUserResponseDto;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createStandardTestUser;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createStandardUserResponseDto;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDtoMapper userDtoMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private static final User STANDARD_TEST_USER = createStandardTestUser();
    private static final User ADMIN_TEST_USER = createAdminTestUser();
    private static final UserResponseDto STANDARD_USER_RESPONSE_DTO = createStandardUserResponseDto();
    private static final UserResponseDto ADMIN_USER_RESPONSE_DTO = createAdminUserResponseDto();

    @Test
    void testGetAllUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        List<User> expectedUsers = List.of(STANDARD_TEST_USER, ADMIN_TEST_USER);
        Page<User> page = new PageImpl<>(expectedUsers);

        when(userRepository.findAll(eq(pageable))).thenReturn(page);
        when(userDtoMapper.mapUserToResponseDto(eq(STANDARD_TEST_USER))).thenReturn(STANDARD_USER_RESPONSE_DTO);
        when(userDtoMapper.mapUserToResponseDto(eq(ADMIN_TEST_USER))).thenReturn(ADMIN_USER_RESPONSE_DTO);

        Page<UserResponseDto> result = userService.getAllUsers(pageable);

        verify(userRepository, times(1)).findAll(eq(pageable));
        assertNotNull(result);
        assertEquals(expectedUsers.size(), result.getContent().size());
        assertTrue(result.getContent().containsAll(List.of(STANDARD_USER_RESPONSE_DTO, ADMIN_USER_RESPONSE_DTO)));
    }

    @Test
    void testGetUserEntityById() {
        when(userRepository.findById(eq(STANDARD_TEST_USER.getId()))).thenReturn(Optional.of(STANDARD_TEST_USER));
        User result = userService.getUserEntityById(STANDARD_TEST_USER.getId());
        verify(userRepository, times(1)).findById(eq(STANDARD_TEST_USER.getId()));
        assertEquals(STANDARD_TEST_USER, result);
    }

    @Test
    void testGetUserEntityByIdForNonExistingUserId() {
        when(userRepository.findById(eq(STANDARD_TEST_USER.getId()))).thenReturn(Optional.empty());
        Exception exception = assertThrows(EntityNotFoundException.class, () -> userService.getUserEntityById(STANDARD_TEST_USER.getId()));
        assertEquals(format(USER_NOT_FOUND_ERROR_MESSAGE, STANDARD_TEST_USER.getId()), exception.getMessage());
    }

    @Test
    void testGetUserById() {
        when(userRepository.findById(eq(STANDARD_TEST_USER.getId()))).thenReturn(Optional.of(STANDARD_TEST_USER));
        when(userDtoMapper.mapUserToResponseDto(eq(STANDARD_TEST_USER))).thenReturn(STANDARD_USER_RESPONSE_DTO);

        UserResponseDto result = userService.getUserById(STANDARD_TEST_USER.getId());

        verify(userRepository, times(1)).findById(eq(STANDARD_TEST_USER.getId()));
        verify(userDtoMapper, times(1)).mapUserToResponseDto(eq(STANDARD_TEST_USER));
        assertEquals(STANDARD_USER_RESPONSE_DTO, result);
    }

    @Test
    void testGetUserByIdForNonExistingUserId() {
        when(userRepository.findById(eq(STANDARD_TEST_USER.getId()))).thenReturn(Optional.empty());
        Exception exception = assertThrows(EntityNotFoundException.class,
            () -> userService.getUserById(STANDARD_TEST_USER.getId()));
        assertEquals(format(USER_NOT_FOUND_ERROR_MESSAGE, STANDARD_TEST_USER.getId()), exception.getMessage());
    }

    @Test
    void testCreateUserForAlreadyExistingEmail() {
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setEmail(STANDARD_TEST_USER.getEmail());
        when(userRepository.findByEmail(eq(STANDARD_TEST_USER.getEmail())))
            .thenReturn(Optional.of(STANDARD_TEST_USER));

        Exception exception = assertThrows(EntityAlreadyExistsException.class,
            () -> userService.createUser(userCreateDto));
        assertEquals(format(USER_WITH_EMAIL_ALREADY_EXISTS_ERROR_MESSAGE, STANDARD_TEST_USER.getEmail()),
            exception.getMessage());
    }

    @Test
    void testCreateUserForAlreadyExistingUsername() {
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setUsername(STANDARD_TEST_USER.getUsername());
        when(userRepository.findByUsername(eq(STANDARD_TEST_USER.getUsername())))
            .thenReturn(Optional.of(STANDARD_TEST_USER));

        Exception exception = assertThrows(EntityAlreadyExistsException.class,
            () -> userService.createUser(userCreateDto));
        assertEquals(format(USER_WITH_USERNAME_ALREADY_EXISTS_ERROR_MESSAGE, STANDARD_TEST_USER.getUsername()),
            exception.getMessage());
    }

    @Test
    void testCreateUser() {
        UserCreateDto userCreateDto = new UserCreateDto();
        when(userDtoMapper.mapDtoToUser(eq(userCreateDto))).thenReturn(STANDARD_TEST_USER);
        when(passwordEncoder.encode(eq(userCreateDto.getPassword()))).thenReturn("encodedPassword");
        when(userRepository.save(eq(STANDARD_TEST_USER))).thenReturn(STANDARD_TEST_USER);
        when(userDtoMapper.mapUserToResponseDto(eq(STANDARD_TEST_USER))).thenReturn(STANDARD_USER_RESPONSE_DTO);

        UserResponseDto result = userService.createUser(userCreateDto);
        verify(userRepository, times(1)).findByUsername(eq(userCreateDto.getUsername()));
        verify(userRepository, times(1)).findByEmail(eq(userCreateDto.getEmail()));
        verify(userDtoMapper, times(1)).mapDtoToUser(eq(userCreateDto));
        verify(passwordEncoder, times(1)).encode(eq(userCreateDto.getPassword()));
        verify(userDtoMapper, times(1)).mapUserToResponseDto(eq(STANDARD_TEST_USER));

        assertEquals(STANDARD_USER_RESPONSE_DTO, result);
    }

    @Test
    void testUpdateUserById() {
        when(userRepository.findById(eq(STANDARD_TEST_USER.getId()))).thenReturn(Optional.of(STANDARD_TEST_USER));
        when(userRepository.save(eq(STANDARD_TEST_USER))).thenReturn(STANDARD_TEST_USER);
        when(userDtoMapper.mapUserToResponseDto(eq(STANDARD_TEST_USER))).thenReturn(STANDARD_USER_RESPONSE_DTO);
        doNothing().when(userDtoMapper).updateUserFromDto(any(UserUpdateDto.class), eq(STANDARD_TEST_USER));

        UserResponseDto result = userService.updateUserById(STANDARD_TEST_USER.getId(), new UserUpdateDto());

        verify(userRepository, times(1)).findById(eq(STANDARD_TEST_USER.getId()));
        verify(userDtoMapper, times(1)).updateUserFromDto(any(UserUpdateDto.class), eq(STANDARD_TEST_USER));
        verify(userRepository, times(1)).save(eq(STANDARD_TEST_USER));
        verify(userDtoMapper, times(1)).mapUserToResponseDto(eq(STANDARD_TEST_USER));

        assertEquals(STANDARD_USER_RESPONSE_DTO, result);
    }

    @Test
    void testDeleteUserById() {
        when(userRepository.findById(eq(STANDARD_TEST_USER.getId()))).thenReturn(Optional.of(STANDARD_TEST_USER));
        when(userDtoMapper.mapUserToResponseDto(eq(STANDARD_TEST_USER))).thenReturn(STANDARD_USER_RESPONSE_DTO);
        doNothing().when(userRepository).deleteById(eq(STANDARD_TEST_USER.getId()));

        UserResponseDto result = userService.deleteUserById(STANDARD_TEST_USER.getId());

        verify(userRepository, times(1)).findById(eq(STANDARD_TEST_USER.getId()));
        verify(userDtoMapper, times(1)).mapUserToResponseDto(eq(STANDARD_TEST_USER));
        verify(userRepository, times(1)).deleteById(eq(STANDARD_TEST_USER.getId()));

        assertEquals(STANDARD_USER_RESPONSE_DTO, result);
    }

    @Test
    void testGetUserByEmail() {
        when(userRepository.findByEmail(eq(STANDARD_TEST_USER.getEmail()))).thenReturn(Optional.of(STANDARD_TEST_USER));
        when(userDtoMapper.mapUserToResponseDto(eq(STANDARD_TEST_USER))).thenReturn(STANDARD_USER_RESPONSE_DTO);

        UserResponseDto result = userService.getUserByEmail(STANDARD_TEST_USER.getEmail());

        verify(userRepository, times(1)).findByEmail(eq(STANDARD_TEST_USER.getEmail()));
        verify(userDtoMapper, times(1)).mapUserToResponseDto(eq(STANDARD_TEST_USER));

        assertEquals(STANDARD_USER_RESPONSE_DTO, result);
    }

    @Test
    void testGetUserByEmailForNonExistingEmail() {
        when(userRepository.findByEmail(eq(STANDARD_TEST_USER.getEmail()))).thenReturn(Optional.empty());
        Exception exception = assertThrows(EntityNotFoundException.class,
            () -> userService.getUserByEmail(STANDARD_TEST_USER.getEmail()));
        assertEquals(format(UserService.USER_WITH_EMAIL_NOT_FOUND_ERROR_MESSAGE, STANDARD_TEST_USER.getEmail()),
            exception.getMessage());
    }

}
