package bg.sofia.uni.fmi.webjava.backend.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RefreshTokenRequest {

    private String refreshToken;

}
