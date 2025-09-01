package bg.sofia.uni.fmi.webjava.backend.controller;

import bg.sofia.uni.fmi.webjava.backend.model.dto.EntityModificationResponse;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.request.EnrollmentRequestResponseDto;
import bg.sofia.uni.fmi.webjava.backend.service.EnrollmentRequestService;
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

import static bg.sofia.uni.fmi.webjava.backend.controller.EnrollmentRequestController.ENROLLMENT_REQUEST_CREATED_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.controller.EnrollmentRequestController.ENROLLMENT_REQUEST_DELETED_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.controller.EnrollmentRequestController.ENROLLMENT_REQUEST_UPDATED_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createTestEnrollmentRequestResponseDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EnrollmentRequestControllerTest {

    @Mock
    private EnrollmentRequestService enrollmentRequestService;

    @InjectMocks
    private EnrollmentRequestController enrollmentRequestController;

    private static final EnrollmentRequestResponseDto
        TEST_ENROLLMENT_REQUEST = createTestEnrollmentRequestResponseDto();
    private static final UUID COURSE_ID = TEST_ENROLLMENT_REQUEST.getCourse().getId();
    private static final UUID USER_ID = TEST_ENROLLMENT_REQUEST.getUser().getId();
    private static final UUID ENROLLMENT_REQUEST_ID = TEST_ENROLLMENT_REQUEST.getId();

    @Test
    void testGetEnrollmentRequestsByCourseId() {
        Page<EnrollmentRequestResponseDto> page = new PageImpl<>(List.of(TEST_ENROLLMENT_REQUEST));
        when(enrollmentRequestService.getEnrollmentRequestsByCourseId(eq(COURSE_ID), any()))
            .thenReturn(page);

        ResponseEntity<Page<EnrollmentRequestResponseDto>> response =
            enrollmentRequestController.getEnrollmentRequestsByCourseId(COURSE_ID, 0, 10, "id", "ASC");

        assertEquals(page, response.getBody());
        verify(enrollmentRequestService).getEnrollmentRequestsByCourseId(eq(COURSE_ID), any());
    }

    @Test
    void testGetEnrollmentRequestsByUserId() {
        Page<EnrollmentRequestResponseDto> page = new PageImpl<>(List.of(TEST_ENROLLMENT_REQUEST));
        when(enrollmentRequestService.getEnrollmentRequestsByUserId(eq(USER_ID), any()))
            .thenReturn(page);

        ResponseEntity<Page<EnrollmentRequestResponseDto>> response =
            enrollmentRequestController.getEnrollmentRequestsByUserId(USER_ID, 0, 10, "id", "ASC");

        assertEquals(page, response.getBody());
        verify(enrollmentRequestService).getEnrollmentRequestsByUserId(eq(USER_ID), any());
    }

    @Test
    void testCreateEnrollmentRequest() {
        when(enrollmentRequestService.createEnrollmentRequest(COURSE_ID, USER_ID))
            .thenReturn(TEST_ENROLLMENT_REQUEST);

        ResponseEntity<EntityModificationResponse<EnrollmentRequestResponseDto>> response =
            enrollmentRequestController.createEnrollmentRequest(COURSE_ID, USER_ID);

        assertNotNull(response.getBody());
        assertEquals(ENROLLMENT_REQUEST_CREATED_MESSAGE, response.getBody().getMessage());
        assertEquals(TEST_ENROLLMENT_REQUEST, response.getBody().getData());
        verify(enrollmentRequestService).createEnrollmentRequest(COURSE_ID, USER_ID);
    }

    @Test
    void testUpdateEnrollmentRequestById() {
        when(enrollmentRequestService.acceptEnrollmentRequestById(ENROLLMENT_REQUEST_ID))
            .thenReturn(TEST_ENROLLMENT_REQUEST);

        ResponseEntity<EntityModificationResponse<EnrollmentRequestResponseDto>> response =
            enrollmentRequestController.updateEnrollmentRequestById(ENROLLMENT_REQUEST_ID);

        assertNotNull(response.getBody());
        assertEquals(ENROLLMENT_REQUEST_UPDATED_MESSAGE, response.getBody().getMessage());
        assertEquals(TEST_ENROLLMENT_REQUEST, response.getBody().getData());
        verify(enrollmentRequestService).acceptEnrollmentRequestById(ENROLLMENT_REQUEST_ID);
    }

    @Test
    void testRejectEnrollmentRequestById() {
        when(enrollmentRequestService.rejectEnrollmentRequestById(ENROLLMENT_REQUEST_ID))
            .thenReturn(TEST_ENROLLMENT_REQUEST);

        ResponseEntity<EntityModificationResponse<EnrollmentRequestResponseDto>> response =
            enrollmentRequestController.rejectEnrollmentRequestById(ENROLLMENT_REQUEST_ID);

        assertNotNull(response.getBody());
        assertEquals(ENROLLMENT_REQUEST_UPDATED_MESSAGE, response.getBody().getMessage());
        assertEquals(TEST_ENROLLMENT_REQUEST, response.getBody().getData());
        verify(enrollmentRequestService).rejectEnrollmentRequestById(ENROLLMENT_REQUEST_ID);
    }

    @Test
    void testDeleteEnrollmentRequestById() {
        when(enrollmentRequestService.deleteEnrollmentRequestById(ENROLLMENT_REQUEST_ID))
            .thenReturn(TEST_ENROLLMENT_REQUEST);

        ResponseEntity<EntityModificationResponse<EnrollmentRequestResponseDto>> response =
            enrollmentRequestController.deleteEnrollmentRequestById(ENROLLMENT_REQUEST_ID);

        assertNotNull(response.getBody());
        assertEquals(ENROLLMENT_REQUEST_DELETED_MESSAGE, response.getBody().getMessage());
        assertEquals(TEST_ENROLLMENT_REQUEST, response.getBody().getData());
        verify(enrollmentRequestService).deleteEnrollmentRequestById(ENROLLMENT_REQUEST_ID);
    }

}
