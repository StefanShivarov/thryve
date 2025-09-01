package bg.sofia.uni.fmi.webjava.backend.controller;

import bg.sofia.uni.fmi.webjava.backend.model.dto.EntityModificationResponse;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.submission.AssignmentSubmissionCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.submission.AssignmentSubmissionGradeDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.submission.AssignmentSubmissionResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.submission.AssignmentSubmissionUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.service.AssignmentSubmissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static bg.sofia.uni.fmi.webjava.backend.controller.AssignmentSubmissionController.SUBMISSION_CREATED_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.controller.AssignmentSubmissionController.SUBMISSION_DELETED_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.controller.AssignmentSubmissionController.SUBMISSION_UPDATED_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createTestAssignmentSubmissionResponseDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AssignmentSubmissionControllerTest {

    @Mock
    private AssignmentSubmissionService submissionService;

    @InjectMocks
    private AssignmentSubmissionController controller;

    private static final AssignmentSubmissionResponseDto
        RESPONSE_DTO = createTestAssignmentSubmissionResponseDto();
    private static final UUID SUBMISSION_ID = RESPONSE_DTO.getId();
    private static final UUID ASSIGNMENT_ID = RESPONSE_DTO.getAssignment().getId();

    @Test
    void testGetSubmissionsByAssignmentId() {
        Page<AssignmentSubmissionResponseDto> page = new PageImpl<>(List.of(RESPONSE_DTO));
        when(submissionService.getSubmissionsByAssignmentId(eq(ASSIGNMENT_ID), any())).thenReturn(page);

        ResponseEntity<Page<AssignmentSubmissionResponseDto>> response =
            controller.getSubmissionsByAssignmentId(ASSIGNMENT_ID, 0, 10, "id", "ASC");

        assertEquals(page, response.getBody());
        verify(submissionService).getSubmissionsByAssignmentId(eq(ASSIGNMENT_ID), any());
    }

    @Test
    void testGetSubmissionById() {
        when(submissionService.getAssignmentSubmissionById(eq(SUBMISSION_ID))).thenReturn(RESPONSE_DTO);

        ResponseEntity<AssignmentSubmissionResponseDto> response =
            controller.getSubmissionById(SUBMISSION_ID);

        assertEquals(RESPONSE_DTO, response.getBody());
        verify(submissionService).getAssignmentSubmissionById(eq(SUBMISSION_ID));
    }

    @Test
    void testCreateSubmission() {
        AssignmentSubmissionCreateDto createDto = new AssignmentSubmissionCreateDto();
        when(submissionService.createAssignmentSubmission(eq(ASSIGNMENT_ID), eq(createDto)))
            .thenReturn(RESPONSE_DTO);

        ResponseEntity<EntityModificationResponse<AssignmentSubmissionResponseDto>> response =
            controller.createSubmission(ASSIGNMENT_ID, createDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(SUBMISSION_CREATED_MESSAGE, response.getBody().getMessage());
        assertEquals(RESPONSE_DTO, response.getBody().getData());
        verify(submissionService).createAssignmentSubmission(eq(ASSIGNMENT_ID), eq(createDto));
    }

    @Test
    void testDeleteSubmissionById() {
        when(submissionService.deleteAssignmentSubmissionById(eq(SUBMISSION_ID)))
            .thenReturn(RESPONSE_DTO);

        ResponseEntity<EntityModificationResponse<AssignmentSubmissionResponseDto>> response =
            controller.deleteSubmissionById(SUBMISSION_ID);

        assertNotNull(response.getBody());
        assertEquals(SUBMISSION_DELETED_MESSAGE, response.getBody().getMessage());
        assertEquals(RESPONSE_DTO, response.getBody().getData());
        verify(submissionService).deleteAssignmentSubmissionById(eq(SUBMISSION_ID));
    }

    @Test
    void testUpdateSubmissionById() {
        AssignmentSubmissionUpdateDto updateDto = new AssignmentSubmissionUpdateDto();
        when(submissionService.updateAssignmentSubmissionById(eq(SUBMISSION_ID), eq(updateDto)))
            .thenReturn(RESPONSE_DTO);

        ResponseEntity<EntityModificationResponse<AssignmentSubmissionResponseDto>> response =
            controller.updateSubmissionById(SUBMISSION_ID, updateDto);

        assertNotNull(response.getBody());
        assertEquals(SUBMISSION_UPDATED_MESSAGE, response.getBody().getMessage());
        assertEquals(RESPONSE_DTO, response.getBody().getData());
        verify(submissionService).updateAssignmentSubmissionById(eq(SUBMISSION_ID), eq(updateDto));
    }

    @Test
    void testGradeSubmissionById() {
        AssignmentSubmissionGradeDto gradeDto = new AssignmentSubmissionGradeDto();
        when(submissionService.gradeAssignmentSubmissionById(eq(SUBMISSION_ID), eq(gradeDto)))
            .thenReturn(RESPONSE_DTO);

        ResponseEntity<EntityModificationResponse<AssignmentSubmissionResponseDto>> response =
            controller.gradeSubmissionById(SUBMISSION_ID, gradeDto);

        assertNotNull(response.getBody());
        assertEquals(SUBMISSION_UPDATED_MESSAGE, response.getBody().getMessage());
        assertEquals(RESPONSE_DTO, response.getBody().getData());
        verify(submissionService).gradeAssignmentSubmissionById(eq(SUBMISSION_ID), eq(gradeDto));
    }

}
