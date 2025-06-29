package bg.sofia.uni.fmi.webjava.backend.controller;

import bg.sofia.uni.fmi.webjava.backend.model.dto.EntityModificationResponse;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.EnrollmentCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.EnrollmentResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.EnrollmentUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private static final String ENROLLMENT_CREATED_MESSAGE = "Enrollment created successfully!";
    private static final String ENROLLMENT_UPDATED_MESSAGE = "Enrollment updated successfully!";
    private static final String ENROLLMENT_DELETED_MESSAGE = "Enrollment deleted successfully!";

    private final EnrollmentService enrollmentService;

    @GetMapping(value = {"", "/"})
    public ResponseEntity<Page<EnrollmentResponseDto>> getEnrollmentsByCourseId(
        @RequestParam(required = false) UUID userId,
        @RequestParam(required = false) UUID courseId,
        @RequestParam(defaultValue = "0") int pageNumber,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String direction
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction).orElse(Sort.Direction.ASC);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sortBy));

        if (userId != null && courseId != null) {
            return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourseIdAndUserId(courseId, userId, pageable));
        }
        if (userId != null) {
            return ResponseEntity.ok(enrollmentService.getEnrollmentsByUserId(userId, pageable));
        }
        if (courseId != null) {
            return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourseId(courseId, pageable));
        }
        return ResponseEntity.ok(enrollmentService.getAllEnrollments(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentResponseDto> getEnrollmentById(@PathVariable UUID id) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentById(id));
    }

    @PostMapping(value = {"", "/"})
    public ResponseEntity<EntityModificationResponse<EnrollmentResponseDto>> createEnrollment(@RequestBody @Valid EnrollmentCreateDto enrollmentCreateDto) {
        return ResponseEntity.ok(
            new EntityModificationResponse<>(ENROLLMENT_CREATED_MESSAGE, enrollmentService.createEnrollment(enrollmentCreateDto))
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EntityModificationResponse<EnrollmentResponseDto>> updateEnrollmentById(@PathVariable UUID id, @RequestBody @Valid EnrollmentUpdateDto enrollmentUpdateDto) {
        return ResponseEntity.ok(
            new EntityModificationResponse<>(ENROLLMENT_UPDATED_MESSAGE, enrollmentService.updateEnrollmentById(id, enrollmentUpdateDto))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EntityModificationResponse<EnrollmentResponseDto>> deleteEnrollmentById(@PathVariable UUID id) {
        return ResponseEntity.ok(
            new EntityModificationResponse<>(ENROLLMENT_DELETED_MESSAGE, enrollmentService.deleteEnrollmentById(id))
        );
    }

}
