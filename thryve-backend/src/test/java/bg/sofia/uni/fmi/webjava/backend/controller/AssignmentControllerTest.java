package bg.sofia.uni.fmi.webjava.backend.controller;

import bg.sofia.uni.fmi.webjava.backend.model.dto.EntityModificationResponse;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.AssignmentCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.AssignmentResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.AssignmentUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.service.AssignmentService;
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

import static bg.sofia.uni.fmi.webjava.backend.controller.AssignmentController.ASSIGNMENT_CREATED_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.controller.AssignmentController.ASSIGNMENT_DELETED_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.controller.AssignmentController.ASSIGNMENT_UPDATED_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createTestAssignmentResponseDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AssignmentControllerTest {

    @Mock
    private AssignmentService assignmentService;

    @InjectMocks
    private AssignmentController controller;

    private static final AssignmentResponseDto RESPONSE_DTO = createTestAssignmentResponseDto();
    private static final UUID ASSIGNMENT_ID = RESPONSE_DTO.getId();
    private static final UUID COURSE_ID = RESPONSE_DTO.getCourse().getId();

    @Test
    void testGetAssignmentsByCourseId() {
        Page<AssignmentResponseDto> page = new PageImpl<>(List.of(RESPONSE_DTO));
        when(assignmentService.getAssignmentsByCourseId(eq(COURSE_ID), any())).thenReturn(page);

        ResponseEntity<Page<AssignmentResponseDto>> response =
            controller.getAssignmentsByCourseId(COURSE_ID, 0, 10, "id", "ASC");

        assertEquals(page, response.getBody());
        verify(assignmentService).getAssignmentsByCourseId(eq(COURSE_ID), any());
    }

    @Test
    void testCreateAssignment() {
        AssignmentCreateDto createDto = new AssignmentCreateDto();
        when(assignmentService.createAssignment(eq(COURSE_ID), eq(createDto)))
            .thenReturn(RESPONSE_DTO);

        ResponseEntity<EntityModificationResponse<AssignmentResponseDto>> response =
            controller.createAssignment(COURSE_ID, createDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ASSIGNMENT_CREATED_MESSAGE, response.getBody().getMessage());
        assertEquals(RESPONSE_DTO, response.getBody().getData());
        verify(assignmentService).createAssignment(eq(COURSE_ID), eq(createDto));
    }

    @Test
    void testUpdateAssignmentById() {
        AssignmentUpdateDto updateDto = new AssignmentUpdateDto();
        when(assignmentService.updateAssignmentById(eq(ASSIGNMENT_ID), eq(updateDto)))
            .thenReturn(RESPONSE_DTO);

        ResponseEntity<EntityModificationResponse<AssignmentResponseDto>> response =
            controller.updateAssignmentById(ASSIGNMENT_ID, updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ASSIGNMENT_UPDATED_MESSAGE, response.getBody().getMessage());
        assertEquals(RESPONSE_DTO, response.getBody().getData());
        verify(assignmentService).updateAssignmentById(eq(ASSIGNMENT_ID), eq(updateDto));
    }

    @Test
    void testDeleteAssignmentById() {
        when(assignmentService.deleteAssignmentById(eq(ASSIGNMENT_ID)))
            .thenReturn(RESPONSE_DTO);

        ResponseEntity<EntityModificationResponse<AssignmentResponseDto>> response =
            controller.deleteAssignmentById(ASSIGNMENT_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ASSIGNMENT_DELETED_MESSAGE, response.getBody().getMessage());
        assertEquals(RESPONSE_DTO, response.getBody().getData());
        verify(assignmentService).deleteAssignmentById(eq(ASSIGNMENT_ID));
    }

}
