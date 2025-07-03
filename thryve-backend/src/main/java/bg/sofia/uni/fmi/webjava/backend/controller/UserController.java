package bg.sofia.uni.fmi.webjava.backend.controller;

import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.EntityModificationResponse;
import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserResponseDto;
import bg.sofia.uni.fmi.webjava.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    public static final String CREATED_USER_MESSAGE = "User created successfully!";
    public static final String UPDATED_USER_MESSAGE = "User updated successfully!";
    public static final String DELETED_USER_MESSAGE = "User deleted successfully!";

    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = {"", "/"})
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
        @RequestParam(defaultValue = "0") int pageNumber,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String direction
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction).orElse(Sort.Direction.ASC);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @PreAuthorize("hasAnyRole('STANDARD', 'CREATOR', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping(value = {"", "/"})
    public ResponseEntity<EntityModificationResponse<UserResponseDto>> registerUser(@RequestBody @Valid UserCreateDto userCreateDto) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(new EntityModificationResponse<>(CREATED_USER_MESSAGE, userService.createUser(userCreateDto)));
    }

    @PreAuthorize("hasAnyRole('STANDARD', 'CREATOR', 'ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<EntityModificationResponse<UserResponseDto>> updateUserById(
        @PathVariable UUID id,
        @RequestBody @Valid UserUpdateDto userUpdateDto
    ) {
        return ResponseEntity.ok(
            new EntityModificationResponse<>(UPDATED_USER_MESSAGE, userService.updateUserById(id, userUpdateDto))
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<EntityModificationResponse<UserResponseDto>> deleteUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(
            new EntityModificationResponse<>(DELETED_USER_MESSAGE, userService.deleteUserById(id))
        );
    }

}
