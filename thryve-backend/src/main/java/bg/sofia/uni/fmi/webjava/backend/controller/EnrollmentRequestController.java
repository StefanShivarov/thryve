package bg.sofia.uni.fmi.webjava.backend.controller;

import bg.sofia.uni.fmi.webjava.backend.model.dto.EntityModificationResponse;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.request.EnrollmentRequestResponseDto;
import bg.sofia.uni.fmi.webjava.backend.service.EnrollmentRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EnrollmentRequestController {

    private static final String ENROLLMENT_REQUEST_CREATED_MESSAGE = "Enrollment request created successfully!";
    private static final String ENROLLMENT_REQUEST_UPDATED_MESSAGE = "Enrollment request updated successfully!";
    private static final String ENROLLMENT_REQUEST_DELETED_MESSAGE = "Enrollment request deleted successfully!";

    private final EnrollmentRequestService enrollmentRequestService;

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @GetMapping("/courses/{courseId}/enrollment-requests")
    public ResponseEntity<Page<EnrollmentRequestResponseDto>> getEnrollmentRequestsByCourseId(
        @PathVariable("courseId") UUID courseId,
        @RequestParam(defaultValue = "0") int pageNumber,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String direction
    ) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.fromOptionalString(direction).orElse(Sort.Direction.ASC), sortBy));
        Page<EnrollmentRequestResponseDto> enrollmentRequests = enrollmentRequestService.getEnrollmentRequestsByCourseId(courseId, pageable);
        return ResponseEntity.ok(enrollmentRequests);
    }

    @PreAuthorize("hasAnyRole('STANDARD', 'CREATOR', 'ADMIN')")
    @GetMapping("/users/{userId}/enrollment-requests")
    public ResponseEntity<Page<EnrollmentRequestResponseDto>> getEnrollmentRequestsByUserId(
        @PathVariable("userId") UUID userId,
        @RequestParam(defaultValue = "0") int pageNumber,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String direction
    ) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.fromOptionalString(direction).orElse(Sort.Direction.ASC), sortBy));
        Page<EnrollmentRequestResponseDto> enrollmentRequests = enrollmentRequestService.getEnrollmentRequestsByUserId(userId, pageable);
        return ResponseEntity.ok(enrollmentRequests);
    }

    @PreAuthorize("hasAnyRole('STANDARD', 'CREATOR', 'ADMIN')")
    @PostMapping("/courses/{courseId}/enrollment-requests")
    public ResponseEntity<EntityModificationResponse<EnrollmentRequestResponseDto>> createEnrollmentRequest(
        @PathVariable("courseId") UUID courseId,
        @RequestParam("userId") UUID userId
    ) {
        EnrollmentRequestResponseDto response = enrollmentRequestService.createEnrollmentRequest(courseId, userId);
        return ResponseEntity.ok(
            new EntityModificationResponse<>(ENROLLMENT_REQUEST_CREATED_MESSAGE, response)
        );
    }

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @PostMapping("/enrollment-requests/{id}/accept")
    public ResponseEntity<EntityModificationResponse<EnrollmentRequestResponseDto>> updateEnrollmentRequestById(
        @PathVariable UUID id
    ) {
        EnrollmentRequestResponseDto response = enrollmentRequestService.acceptEnrollmentRequestById(id);
        return ResponseEntity.ok(
            new EntityModificationResponse<>(ENROLLMENT_REQUEST_UPDATED_MESSAGE, response)
        );
    }

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @PostMapping("/enrollment-requests/{id}/reject")
    public ResponseEntity<EntityModificationResponse<EnrollmentRequestResponseDto>> rejectEnrollmentRequestById(
        @PathVariable UUID id
    ) {
        EnrollmentRequestResponseDto response = enrollmentRequestService.rejectEnrollmentRequestById(id);
        return ResponseEntity.ok(
            new EntityModificationResponse<>(ENROLLMENT_REQUEST_UPDATED_MESSAGE, response)
        );
    }

    @PreAuthorize("hasAnyRole('STANDARD', 'CREATOR', 'ADMIN')")
    @DeleteMapping("/enrollment-requests/{id}")
    public ResponseEntity<EntityModificationResponse<EnrollmentRequestResponseDto>> deleteEnrollmentRequestById(
        @PathVariable UUID id
    ) {
        EnrollmentRequestResponseDto response = enrollmentRequestService.deleteEnrollmentRequestById(id);
        return ResponseEntity.ok(
            new EntityModificationResponse<>(ENROLLMENT_REQUEST_DELETED_MESSAGE, response)
        );
    }

}
