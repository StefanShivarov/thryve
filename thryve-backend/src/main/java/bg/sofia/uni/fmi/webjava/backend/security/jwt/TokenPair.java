package bg.sofia.uni.fmi.webjava.backend.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenPair {

    private String accessToken;
    private String refreshToken;

}
