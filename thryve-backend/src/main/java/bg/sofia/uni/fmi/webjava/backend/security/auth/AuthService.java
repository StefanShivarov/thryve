package bg.sofia.uni.fmi.webjava.backend.security.auth;

import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserLoginDto;
import bg.sofia.uni.fmi.webjava.backend.repository.UserRepository;
import bg.sofia.uni.fmi.webjava.backend.security.jwt.JwtService;
import bg.sofia.uni.fmi.webjava.backend.security.jwt.RefreshTokenRequest;
import bg.sofia.uni.fmi.webjava.backend.security.jwt.TokenPair;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public TokenPair login(@Valid UserLoginDto userLoginDto) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(userLoginDto.getEmail(), userLoginDto.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtService.generateTokenPair(authentication);
    }

    public TokenPair refreshToken(@Valid RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token!");
        }

        String username = jwtService.extractUsernameFromToken(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (userDetails == null) {
            throw new IllegalArgumentException("Invalid username or password!");
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        String accessToken = jwtService.generateAccessToken(authenticationToken);
        return new TokenPair(accessToken, refreshToken);
    }
}
