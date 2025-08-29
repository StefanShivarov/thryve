package bg.sofia.uni.fmi.webjava.backend.repository;

import bg.sofia.uni.fmi.webjava.backend.model.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Course;
import java.util.List;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    List<Enrollment> findByCourse(Course course);

    Page<Enrollment> findEnrollmentByCourseIdAndUserId(UUID courseId, UUID userId, Pageable pageable);

    Page<Enrollment> findEnrollmentsByCourseId(UUID courseId, Pageable pageable);

    Page<Enrollment> findEnrollmentsByUserId(UUID userId, Pageable pageable);

}
