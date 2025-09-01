package bg.sofia.uni.fmi.webjava.backend.controller;

import bg.sofia.uni.fmi.webjava.backend.model.dto.EntityModificationResponse;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.EnrollmentCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.EnrollmentResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.EnrollmentUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.EnrollmentType;
import bg.sofia.uni.fmi.webjava.backend.service.EnrollmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static bg.sofia.uni.fmi.webjava.backend.controller.EnrollmentController.ENROLLMENT_CREATED_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.controller.EnrollmentController.ENROLLMENT_DELETED_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.controller.EnrollmentController.ENROLLMENT_UPDATED_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createTestEnrollmentResponseDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EnrollmentControllerTest {

    @Mock
    private EnrollmentService enrollmentService;

    @InjectMocks
    private EnrollmentController enrollmentController;

    private static final EnrollmentResponseDto
        TEST_ENROLLMENT_RESPONSE_DTO = createTestEnrollmentResponseDto();
    private static final UUID ENROLLMENT_ID = TEST_ENROLLMENT_RESPONSE_DTO.getId();
    private static final UUID USER_ID = TEST_ENROLLMENT_RESPONSE_DTO.getUser().getId();
    private static final UUID COURSE_ID = TEST_ENROLLMENT_RESPONSE_DTO.getCourse().getId();
    private static final EnrollmentCreateDto CREATE_DTO = new EnrollmentCreateDto(
        USER_ID, COURSE_ID, EnrollmentType.STUDENT);

    @Test
    void testGetEnrollmentsByCourseIdAndUserId() {
        Page<EnrollmentResponseDto> page = new PageImpl<>(List.of(TEST_ENROLLMENT_RESPONSE_DTO));
        when(enrollmentService.getEnrollmentsByCourseIdAndUserId(eq(COURSE_ID), eq(USER_ID), any()))
            .thenReturn(page);

        ResponseEntity<Page<EnrollmentResponseDto>> response = enrollmentController
            .getEnrollmentsByCourseId(USER_ID, COURSE_ID, 0, 10, "id", "ASC");

        assertEquals(page, response.getBody());
        verify(enrollmentService).getEnrollmentsByCourseIdAndUserId(eq(COURSE_ID), eq(USER_ID), any());
    }

    @Test
    void testGetEnrollmentsByUserId() {
        Page<EnrollmentResponseDto> page = new PageImpl<>(List.of(TEST_ENROLLMENT_RESPONSE_DTO));
        when(enrollmentService.getEnrollmentsByUserId(eq(USER_ID), any()))
            .thenReturn(page);

        ResponseEntity<Page<EnrollmentResponseDto>> response = enrollmentController
            .getEnrollmentsByCourseId(USER_ID, null, 0, 10, "id", "ASC");

        assertEquals(page, response.getBody());
        verify(enrollmentService).getEnrollmentsByUserId(eq(USER_ID), any());
    }

    @Test
    void testGetEnrollmentsByCourseIdOnly() {
        Page<EnrollmentResponseDto> page = new PageImpl<>(List.of(TEST_ENROLLMENT_RESPONSE_DTO));
        when(enrollmentService.getEnrollmentsByCourseId(eq(COURSE_ID), any()))
            .thenReturn(page);

        ResponseEntity<Page<EnrollmentResponseDto>> response = enrollmentController
            .getEnrollmentsByCourseId(null, COURSE_ID, 0, 10, "id", "ASC");

        assertEquals(page, response.getBody());
        verify(enrollmentService).getEnrollmentsByCourseId(eq(COURSE_ID), any());
    }

    @Test
    void testGetAllEnrollments() {
        Page<EnrollmentResponseDto> page = new PageImpl<>(List.of(TEST_ENROLLMENT_RESPONSE_DTO));
        when(enrollmentService.getAllEnrollments(any())).thenReturn(page);

        ResponseEntity<Page<EnrollmentResponseDto>> response = enrollmentController
            .getEnrollmentsByCourseId(null, null, 0, 10, "id", "ASC");

        assertEquals(page, response.getBody());
        verify(enrollmentService).getAllEnrollments(any());
    }

    @Test
    void testGetEnrollmentById() {
        when(enrollmentService.getEnrollmentById(ENROLLMENT_ID)).thenReturn(TEST_ENROLLMENT_RESPONSE_DTO);

        ResponseEntity<EnrollmentResponseDto> response = enrollmentController
            .getEnrollmentById(ENROLLMENT_ID);

        assertEquals(TEST_ENROLLMENT_RESPONSE_DTO, response.getBody());
        verify(enrollmentService).getEnrollmentById(ENROLLMENT_ID);
    }

    @Test
    void testCreateEnrollment() {
        when(enrollmentService.createEnrollment(eq(CREATE_DTO))).thenReturn(TEST_ENROLLMENT_RESPONSE_DTO);

        ResponseEntity<EntityModificationResponse<EnrollmentResponseDto>> response = enrollmentController
            .createEnrollment(CREATE_DTO);

        assertNotNull(response.getBody());
        assertEquals(ENROLLMENT_CREATED_MESSAGE, response.getBody().getMessage());
        assertEquals(TEST_ENROLLMENT_RESPONSE_DTO, response.getBody().getData());
        verify(enrollmentService).createEnrollment(eq(CREATE_DTO));
    }

    @Test
    void testUpdateEnrollmentById() {
        EnrollmentUpdateDto updateDto = new EnrollmentUpdateDto();
        when(enrollmentService.updateEnrollmentById(eq(ENROLLMENT_ID), eq(updateDto)))
            .thenReturn(TEST_ENROLLMENT_RESPONSE_DTO);

        ResponseEntity<EntityModificationResponse<EnrollmentResponseDto>> response = enrollmentController
            .updateEnrollmentById(ENROLLMENT_ID, updateDto);

        assertNotNull(response.getBody());
        assertEquals(ENROLLMENT_UPDATED_MESSAGE, response.getBody().getMessage());
        assertEquals(TEST_ENROLLMENT_RESPONSE_DTO, response.getBody().getData());
        verify(enrollmentService).updateEnrollmentById(eq(ENROLLMENT_ID), eq(updateDto));
    }

    @Test
    void testDeleteEnrollmentById() {
        when(enrollmentService.deleteEnrollmentById(eq(ENROLLMENT_ID)))
            .thenReturn(TEST_ENROLLMENT_RESPONSE_DTO);

        ResponseEntity<EntityModificationResponse<EnrollmentResponseDto>> response = enrollmentController
            .deleteEnrollmentById(ENROLLMENT_ID);

        assertNotNull(response.getBody());
        assertEquals(ENROLLMENT_DELETED_MESSAGE, response.getBody().getMessage());
        assertEquals(TEST_ENROLLMENT_RESPONSE_DTO, response.getBody().getData());
        verify(enrollmentService).deleteEnrollmentById(eq(ENROLLMENT_ID));
    }

}
