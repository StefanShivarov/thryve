package bg.sofia.uni.fmi.webjava.backend.controller;

import bg.sofia.uni.fmi.webjava.backend.model.dto.EntityModificationResponse;
import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createStandardUserResponseDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private static final UserResponseDto TEST_USER_RESPONSE_DTO = createStandardUserResponseDto();
    private static final UUID USER_ID = TEST_USER_RESPONSE_DTO.getId();

    @Test
    void testGetAllUsers() {
        Page<UserResponseDto> page = new PageImpl<>(List.of(TEST_USER_RESPONSE_DTO));
        when(userService.getAllUsers(any())).thenReturn(page);

        ResponseEntity<Page<UserResponseDto>> response = userController
            .getAllUsers(0, 10, "id", "ASC");

        assertEquals(page, response.getBody());
        verify(userService).getAllUsers(any());
    }

    @Test
    void testGetUserById() {
        when(userService.getUserById(eq(USER_ID))).thenReturn(TEST_USER_RESPONSE_DTO);

        ResponseEntity<UserResponseDto> response = userController.getUserById(USER_ID);

        assertEquals(TEST_USER_RESPONSE_DTO, response.getBody());
        verify(userService).getUserById(eq(USER_ID));
    }

    @Test
    void testRegisterUser() {
        UserCreateDto createDto = new UserCreateDto(
            TEST_USER_RESPONSE_DTO.getUsername(),
            TEST_USER_RESPONSE_DTO.getEmail(),
            "Test12345",
            TEST_USER_RESPONSE_DTO.getFirstName(),
            TEST_USER_RESPONSE_DTO.getLastName()
        );
        when(userService.createUser(eq(createDto))).thenReturn(TEST_USER_RESPONSE_DTO);

        ResponseEntity<EntityModificationResponse<UserResponseDto>> response = userController
            .registerUser(createDto);

        Assertions.assertNotNull(response.getBody());
        assertEquals(UserController.CREATED_USER_MESSAGE, response.getBody().getMessage());
        assertEquals(TEST_USER_RESPONSE_DTO, response.getBody().getData());
        verify(userService).createUser(createDto);
    }

    @Test
    void testUpdateUserById() {
        UserUpdateDto updateDto = new UserUpdateDto();
        when(userService.updateUserById(eq(USER_ID), eq(updateDto)))
            .thenReturn(TEST_USER_RESPONSE_DTO);

        ResponseEntity<EntityModificationResponse<UserResponseDto>> response = userController
            .updateUserById(USER_ID, updateDto);

        Assertions.assertNotNull(response.getBody());
        assertEquals(UserController.UPDATED_USER_MESSAGE, response.getBody().getMessage());
        assertEquals(TEST_USER_RESPONSE_DTO, response.getBody().getData());
        verify(userService).updateUserById(USER_ID, updateDto);
    }

    @Test
    void testDeleteUserById() {
        when(userService.deleteUserById(eq(USER_ID))).thenReturn(TEST_USER_RESPONSE_DTO);

        ResponseEntity<EntityModificationResponse<UserResponseDto>> response = userController
            .deleteUserById(USER_ID);

        Assertions.assertNotNull(response.getBody());
        assertEquals(UserController.DELETED_USER_MESSAGE, response.getBody().getMessage());
        assertEquals(TEST_USER_RESPONSE_DTO, response.getBody().getData());
        verify(userService).deleteUserById(USER_ID);
    }

    @Test
    void testGetCurrentlyLoggedUser() {
        Authentication auth = mock();
        when(auth.getName()).thenReturn(TEST_USER_RESPONSE_DTO.getEmail());
        when(userService.getUserByEmail(eq(TEST_USER_RESPONSE_DTO.getEmail())))
            .thenReturn(TEST_USER_RESPONSE_DTO);

        ResponseEntity<UserResponseDto> response = userController.me(auth);

        assertEquals(TEST_USER_RESPONSE_DTO, response.getBody());
        verify(userService).getUserByEmail(eq(TEST_USER_RESPONSE_DTO.getEmail()));
    }

}
