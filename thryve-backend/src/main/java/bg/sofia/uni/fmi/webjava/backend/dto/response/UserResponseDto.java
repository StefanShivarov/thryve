package bg.sofia.uni.fmi.webjava.backend.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class UserResponseDto {

    private UUID id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;

}
