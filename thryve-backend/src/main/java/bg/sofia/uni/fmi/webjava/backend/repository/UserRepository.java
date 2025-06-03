package bg.sofia.uni.fmi.webjava.backend.repository;

import bg.sofia.uni.fmi.webjava.backend.model.entity.EnrollmentState;
import bg.sofia.uni.fmi.webjava.backend.model.entity.EnrollmentType;
import bg.sofia.uni.fmi.webjava.backend.model.entity.User;
import bg.sofia.uni.fmi.webjava.backend.model.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    List<User> findAllByRole(UserRole role);

    List<User> findAllByEnrollmentType(EnrollmentType enrollmentType);

    List<User> findAllByEnrollmentState(EnrollmentState enrollmentState);

    List<User> findAllByRoleAndEnrollmentType(UserRole role, EnrollmentType enrollmentType);

    List<User> findAllByRoleAndEnrollmentState(UserRole role, EnrollmentState enrollmentState);
}
