package bg.sofia.uni.fmi.webjava.backend.repository;

import bg.sofia.uni.fmi.webjava.backend.model.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

}
