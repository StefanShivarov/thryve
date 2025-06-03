package bg.sofia.uni.fmi.webjava.backend.service.impl;

import bg.sofia.uni.fmi.webjava.backend.dto.RegisterUserDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.User;
import bg.sofia.uni.fmi.webjava.backend.model.entity.UserRole;
import bg.sofia.uni.fmi.webjava.backend.repository.UserRepository;
import bg.sofia.uni.fmi.webjava.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User registerUser(RegisterUserDto registerUserDto) {
        User user = new User();
        user.setUsername(registerUserDto.getUsername());
        user.setEmail(registerUserDto.getEmail());
        user.setRole(UserRole.STANDARD);
        user.setPassword(registerUserDto.getPassword()); // crypt password
        user.setEnrollments(new HashSet<>());
        return userRepository.save(user);
    }
}
