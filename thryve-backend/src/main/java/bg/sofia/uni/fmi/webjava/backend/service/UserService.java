package bg.sofia.uni.fmi.webjava.backend.service;

import bg.sofia.uni.fmi.webjava.backend.dto.RegisterUserDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.User;

public interface UserService {

    public User registerUser(RegisterUserDto registerUserDto);
}
