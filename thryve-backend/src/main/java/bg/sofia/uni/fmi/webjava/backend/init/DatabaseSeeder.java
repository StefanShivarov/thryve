package bg.sofia.uni.fmi.webjava.backend.init;

import bg.sofia.uni.fmi.webjava.backend.model.entity.User;
import bg.sofia.uni.fmi.webjava.backend.model.entity.UserRole;
import bg.sofia.uni.fmi.webjava.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${thryve.admin.password}")
    private String adminPassword;

    private void seedAdminUserIfNeeded() {
        if (userRepository.count() != 0) {
            return;
        }

        User admin = new User();
        admin.setUsername("thryveAdmin");
        admin.setFirstName("Thryve");
        admin.setLastName("Admin");
        admin.setEmail("thryve@mail.cx");
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(UserRole.ADMIN);

        userRepository.save(admin);
    }

    @Override
    public void run(String... args) throws Exception {
        seedAdminUserIfNeeded();
    }

}
