package bg.sofia.uni.fmi.webjava.backend.controller;

import bg.sofia.uni.fmi.webjava.backend.dto.RegisterUserDto;
import bg.sofia.uni.fmi.webjava.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid RegisterUserDto userDto) {
        userService.registerUser(userDto);
        return ResponseEntity.ok("User registered successfully");
    }
}
