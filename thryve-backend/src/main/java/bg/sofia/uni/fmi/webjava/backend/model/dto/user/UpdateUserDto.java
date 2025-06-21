package bg.sofia.uni.fmi.webjava.backend.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserDto {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters long")
    private String username;

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters long")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters long")
    private String lastName;

    @Email(message = "Invalid email format provided!")
    private String email;

}
