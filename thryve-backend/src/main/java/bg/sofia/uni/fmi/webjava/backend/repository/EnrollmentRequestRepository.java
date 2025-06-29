package bg.sofia.uni.fmi.webjava.backend.repository;

import bg.sofia.uni.fmi.webjava.backend.model.entity.EnrollmentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRequestRepository extends JpaRepository<EnrollmentRequest, UUID> {

    Page<EnrollmentRequest> findEnrollmentRequestsByCourseId(UUID courseId, Pageable pageable);

    Page<EnrollmentRequest> findEnrollmentRequestsByUserId(UUID userId, Pageable pageable);

    Optional<EnrollmentRequest> findEnrollmentRequestByCourseIdAndUserId(UUID courseId, UUID userId);

}
