package bg.sofia.uni.fmi.webjava.backend.controller;

import bg.sofia.uni.fmi.webjava.backend.model.dto.EntityModificationResponse;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.AssignmentCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.AssignmentResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.AssignmentUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.service.AssignmentService;
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
@RequiredArgsConstructor
@RequestMapping("/api")
public class AssignmentController {

    private static final String ASSIGNMENT_CREATED_MESSAGE = "Assignment created successfully!";
    private static final String ASSIGNMENT_UPDATED_MESSAGE = "Assignment updated successfully!";
    private static final String ASSIGNMENT_DELETED_MESSAGE = "Assignment deleted successfully!";

    private final AssignmentService assignmentService;

    @GetMapping("/courses/{courseId}/assignments")
    public ResponseEntity<Page<AssignmentResponseDto>> getAssignmentsByCourseId(
        @PathVariable("courseId") UUID courseId,
        @RequestParam(defaultValue = "0") int pageNumber,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String direction
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction)
            .orElse(Sort.Direction.ASC);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sortBy));
        Page<AssignmentResponseDto> enrollmentRequests =
            assignmentService.getAssignmentsByCourseId(courseId, pageable);
        return ResponseEntity.ok(enrollmentRequests);
    }

    @PostMapping("/courses/{courseId}/assignments")
    public ResponseEntity<EntityModificationResponse<AssignmentResponseDto>> createAssignment(
        @PathVariable("courseId") UUID courseId,
        @RequestBody @Valid AssignmentCreateDto assignmentCreateDto
    ) {
        AssignmentResponseDto response = assignmentService.createAssignment(courseId, assignmentCreateDto);
        return ResponseEntity.ok(
            new EntityModificationResponse<>(ASSIGNMENT_CREATED_MESSAGE, response)
        );
    }

    @PatchMapping("/assignments/{id}")
    public ResponseEntity<EntityModificationResponse<AssignmentResponseDto>> updateAssignmentById(
        @PathVariable UUID id,
        @RequestBody @Valid AssignmentUpdateDto assignmentUpdateDto
    ) {
        AssignmentResponseDto response = assignmentService.updateAssignmentById(id, assignmentUpdateDto);
        return ResponseEntity.ok(
            new EntityModificationResponse<>(ASSIGNMENT_UPDATED_MESSAGE, response)
        );
    }

    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<EntityModificationResponse<AssignmentResponseDto>> deleteAssignmentById(@PathVariable UUID id) {
        AssignmentResponseDto response = assignmentService.deleteAssignmentById(id);
        return ResponseEntity.ok(
            new EntityModificationResponse<>(ASSIGNMENT_DELETED_MESSAGE, response)
        );
    }

}
