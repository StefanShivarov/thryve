package bg.sofia.uni.fmi.webjava.backend.utils;

import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.User;
import bg.sofia.uni.fmi.webjava.backend.model.entity.UserRole;

import java.util.UUID;

public class TestUtils {

    private static final UUID STANDARD_USER_ID = UUID.randomUUID();
    private static final String STANDARD_USER_USERNAME = "testUser";
    private static final String STANDARD_USER_EMAIL = "test@example.com";
    private static final String STANDARD_USER_PASSWORD = "testpass";
    private static final String STANDARD_USER_FIRST_NAME = "Test";

    private static final UUID ADMIN_USER_ID = UUID.randomUUID();
    private static final String ADMIN_USER_USERNAME = "adminUser";
    private static final String ADMIN_USER_EMAIL = "admin@example.com";
    private static final String ADMIN_USER_PASSWORD = "adminpass";
    private static final String ADMIN_USER_FIRST_NAME = "Admin";
    private static final String USER_LAST_NAME = "User";

    public static User createStandardTestUser() {
        User user = new User();
        user.setId(STANDARD_USER_ID);
        user.setFirstName(STANDARD_USER_FIRST_NAME);
        user.setLastName(USER_LAST_NAME);
        user.setUsername(STANDARD_USER_USERNAME);
        user.setEmail(STANDARD_USER_EMAIL);
        user.setPassword(STANDARD_USER_PASSWORD);
        user.setRole(UserRole.STANDARD);
        return user;
    }

    public static User createAdminTestUser() {
        User admin = new User();
        admin.setId(ADMIN_USER_ID);
        admin.setFirstName(ADMIN_USER_FIRST_NAME);
        admin.setLastName(USER_LAST_NAME);
        admin.setUsername(ADMIN_USER_USERNAME);
        admin.setEmail(ADMIN_USER_EMAIL);
        admin.setPassword(ADMIN_USER_PASSWORD);
        admin.setRole(UserRole.ADMIN);
        return admin;
    }

    public static UserResponseDto createStandardUserResponseDto() {
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(STANDARD_USER_ID);
        userResponseDto.setFirstName(STANDARD_USER_FIRST_NAME);
        userResponseDto.setLastName(USER_LAST_NAME);
        userResponseDto.setUsername(STANDARD_USER_USERNAME);
        userResponseDto.setEmail(STANDARD_USER_EMAIL);
        return userResponseDto;
    }

    public static UserResponseDto createAdminUserResponseDto() {
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(ADMIN_USER_ID);
        userResponseDto.setFirstName(ADMIN_USER_FIRST_NAME);
        userResponseDto.setLastName(USER_LAST_NAME);
        userResponseDto.setUsername(ADMIN_USER_USERNAME);
        userResponseDto.setEmail(ADMIN_USER_EMAIL);
        return userResponseDto;
    }

}
