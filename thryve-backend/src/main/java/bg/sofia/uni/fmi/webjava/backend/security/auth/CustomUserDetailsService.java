package bg.sofia.uni.fmi.webjava.backend.security.auth;

import bg.sofia.uni.fmi.webjava.backend.model.entity.User;
import bg.sofia.uni.fmi.webjava.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            getAuthority(user)
        );
    }

    private Collection<? extends GrantedAuthority> getAuthority(User user) {
        return List.of(new SimpleGrantedAuthority(format("ROLE_%s", user.getRole().name())));
    }

}
