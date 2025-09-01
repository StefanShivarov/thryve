package bg.sofia.uni.fmi.webjava.backend.controller;

import bg.sofia.uni.fmi.webjava.backend.model.dto.EntityModificationResponse;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.submission.AssignmentSubmissionCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.submission.AssignmentSubmissionGradeDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.submission.AssignmentSubmissionResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.submission.AssignmentSubmissionUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.service.AssignmentSubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequiredArgsConstructor
@RequestMapping("/api")
public class AssignmentSubmissionController {

    public static final String SUBMISSION_CREATED_MESSAGE = "Submission created successfully!";
    public static final String SUBMISSION_DELETED_MESSAGE = "Submission deleted successfully!";
    public static final String SUBMISSION_UPDATED_MESSAGE = "Submission updated successfully!";

    private final AssignmentSubmissionService assignmentSubmissionService;

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @GetMapping("/assignments/{assignmentId}/submissions")
    public ResponseEntity<Page<AssignmentSubmissionResponseDto>> getSubmissionsByAssignmentId(
        @PathVariable("assignmentId") UUID assignmentId,
        @RequestParam(defaultValue = "0") int pageNumber,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String direction
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction)
            .orElse(Sort.Direction.ASC);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sortBy));
        Page<AssignmentSubmissionResponseDto> submissions =
            assignmentSubmissionService.getSubmissionsByAssignmentId(assignmentId, pageable);
        return ResponseEntity.ok(submissions);
    }

    @PreAuthorize("hasAnyRole('STANDARD', 'CREATOR', 'ADMIN')")
    @GetMapping("/submissions/{id}")
    public ResponseEntity<AssignmentSubmissionResponseDto> getSubmissionById(@PathVariable UUID id) {
        return ResponseEntity.ok(assignmentSubmissionService.getAssignmentSubmissionById(id));
    }

    @PreAuthorize("hasAnyRole('STANDARD', 'CREATOR', 'ADMIN')")
    @PostMapping("/assignments/{assignmentId}/submissions")
    public ResponseEntity<EntityModificationResponse<AssignmentSubmissionResponseDto>> createSubmission(
        @PathVariable("assignmentId") UUID assignmentId,
        @RequestBody @Valid AssignmentSubmissionCreateDto submissionCreateDto
    ) {
        return ResponseEntity.status(201).body(
            new EntityModificationResponse<>(SUBMISSION_CREATED_MESSAGE,
                assignmentSubmissionService.createAssignmentSubmission(assignmentId, submissionCreateDto))
        );
    }

    @PreAuthorize("hasAnyRole('STANDARD', 'CREATOR', 'ADMIN')")
    @DeleteMapping("/submissions/{id}")
    public ResponseEntity<EntityModificationResponse<AssignmentSubmissionResponseDto>> deleteSubmissionById(
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
            new EntityModificationResponse<>(SUBMISSION_DELETED_MESSAGE,
                assignmentSubmissionService.deleteAssignmentSubmissionById(id))
        );
    }

    @PreAuthorize("hasAnyRole('STANDARD', 'CREATOR', 'ADMIN')")
    @PatchMapping("/submissions/{id}")
    public ResponseEntity<EntityModificationResponse<AssignmentSubmissionResponseDto>> updateSubmissionById(
        @PathVariable UUID id,
        @RequestBody @Valid AssignmentSubmissionUpdateDto submissionUpdateDto
    ) {
        return ResponseEntity.ok(
            new EntityModificationResponse<>(SUBMISSION_UPDATED_MESSAGE,
                assignmentSubmissionService.updateAssignmentSubmissionById(id, submissionUpdateDto))
        );
    }

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @PatchMapping("/submissions/{id}/grade")
    public ResponseEntity<EntityModificationResponse<AssignmentSubmissionResponseDto>> gradeSubmissionById(
        @PathVariable UUID id,
        @RequestBody @Valid AssignmentSubmissionGradeDto submissionGradeDto
    ) {
        return ResponseEntity.ok(
            new EntityModificationResponse<>(SUBMISSION_UPDATED_MESSAGE,
                assignmentSubmissionService.gradeAssignmentSubmissionById(id, submissionGradeDto))
        );
    }

}
