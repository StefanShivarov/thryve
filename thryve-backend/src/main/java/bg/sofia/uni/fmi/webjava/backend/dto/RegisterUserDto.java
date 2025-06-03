package bg.sofia.uni.fmi.webjava.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUserDto {
    private String username;

    @Email
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit")
    private String password;


    private String firstName;
    private String lastName;

    public RegisterUserDto() {
    }

    public RegisterUserDto(String username, String email, String password, String firstName, String lastName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
