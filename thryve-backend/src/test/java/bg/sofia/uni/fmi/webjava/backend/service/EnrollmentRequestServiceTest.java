package bg.sofia.uni.fmi.webjava.backend.service;

import bg.sofia.uni.fmi.webjava.backend.exception.EnrollmentRequestAlreadyFinalizedException;
import bg.sofia.uni.fmi.webjava.backend.exception.EntityAlreadyExistsException;
import bg.sofia.uni.fmi.webjava.backend.exception.EntityNotFoundException;
import bg.sofia.uni.fmi.webjava.backend.mapper.EnrollmentRequestDtoMapper;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.EnrollmentCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.EnrollmentResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.request.EnrollmentRequestResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.EnrollmentRequest;
import bg.sofia.uni.fmi.webjava.backend.model.entity.EnrollmentState;
import bg.sofia.uni.fmi.webjava.backend.repository.EnrollmentRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static bg.sofia.uni.fmi.webjava.backend.service.EnrollmentRequestService.ENROLLMENT_REQUEST_ALREADY_EXISTS_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.service.EnrollmentRequestService.ENROLLMENT_REQUEST_NOT_FOUND_ERROR_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createTestEnrollmentRequest;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createTestEnrollmentRequestResponseDto;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EnrollmentRequestServiceTest {

    @Mock
    private EnrollmentRequestRepository enrollmentRequestRepository;

    @Mock
    private EnrollmentRequestDtoMapper enrollmentRequestDtoMapper;

    @Mock
    private CourseService courseService;

    @Mock
    private UserService userService;

    @Mock
    private EnrollmentService enrollmentService;

    @InjectMocks
    private EnrollmentRequestService enrollmentRequestService;

    private static final EnrollmentRequest TEST_ENROLLMENT_REQUEST = createTestEnrollmentRequest();
    private static final EnrollmentRequestResponseDto
        TEST_ENROLLMENT_REQUEST_RESPONSE_DTO = createTestEnrollmentRequestResponseDto();

    @Test
    void testGetEnrollmentRequestsByUserId() {
        Pageable pageable = PageRequest.of(0, 10);
        List<EnrollmentRequest> expectedEnrollmentRequests = List.of(TEST_ENROLLMENT_REQUEST);
        Page<EnrollmentRequest> page = new PageImpl<>(expectedEnrollmentRequests);

        when(enrollmentRequestRepository.findEnrollmentRequestsByUserId(
            eq(TEST_ENROLLMENT_REQUEST.getUser().getId()), eq(pageable)))
            .thenReturn(page);
        when(enrollmentRequestDtoMapper.mapToResponseDto(eq(TEST_ENROLLMENT_REQUEST)))
            .thenReturn(TEST_ENROLLMENT_REQUEST_RESPONSE_DTO);

        Page<EnrollmentRequestResponseDto> result = enrollmentRequestService
            .getEnrollmentRequestsByUserId(TEST_ENROLLMENT_REQUEST_RESPONSE_DTO.getUser().getId(), pageable);

        verify(enrollmentRequestRepository, times(1))
            .findEnrollmentRequestsByUserId(eq(TEST_ENROLLMENT_REQUEST.getUser().getId()), eq(pageable));
        assertNotNull(result);
        assertEquals(expectedEnrollmentRequests.size(), result.getContent().size());
        assertTrue(result.getContent().contains(TEST_ENROLLMENT_REQUEST_RESPONSE_DTO));
    }

    @Test
    void testGetEnrollmentsByCourseId() {
        Pageable pageable = PageRequest.of(0, 10);
        List<EnrollmentRequest> expectedEnrollmentRequests = List.of(TEST_ENROLLMENT_REQUEST);
        Page<EnrollmentRequest> page = new PageImpl<>(expectedEnrollmentRequests);

        when(enrollmentRequestRepository.findEnrollmentRequestsByCourseId(
            eq(TEST_ENROLLMENT_REQUEST.getCourse().getId()), eq(pageable)))
            .thenReturn(page);
        when(enrollmentRequestDtoMapper.mapToResponseDto(eq(TEST_ENROLLMENT_REQUEST)))
            .thenReturn(TEST_ENROLLMENT_REQUEST_RESPONSE_DTO);

        Page<EnrollmentRequestResponseDto> result = enrollmentRequestService
            .getEnrollmentRequestsByCourseId(TEST_ENROLLMENT_REQUEST_RESPONSE_DTO.getCourse().getId(), pageable);

        verify(enrollmentRequestRepository, times(1))
            .findEnrollmentRequestsByCourseId(eq(TEST_ENROLLMENT_REQUEST.getCourse().getId()), eq(pageable));
        assertNotNull(result);
        assertEquals(expectedEnrollmentRequests.size(), result.getContent().size());
        assertTrue(result.getContent().contains(TEST_ENROLLMENT_REQUEST_RESPONSE_DTO));
    }

    @Test
    void testGetEnrollmentRequestEntityById() {
        when(enrollmentRequestRepository.findById(eq(TEST_ENROLLMENT_REQUEST.getId())))
            .thenReturn(Optional.of(TEST_ENROLLMENT_REQUEST));
        EnrollmentRequest result = enrollmentRequestService
            .getEnrollmentRequestEntityById(TEST_ENROLLMENT_REQUEST.getId());
        verify(enrollmentRequestRepository, times(1))
            .findById(TEST_ENROLLMENT_REQUEST.getId());
        assertEquals(TEST_ENROLLMENT_REQUEST, result);
    }

    @Test
    void testGetEnrollmentRequestEntityByIdForNonExistingId() {
        when(enrollmentRequestRepository.findById(eq(TEST_ENROLLMENT_REQUEST.getId())))
            .thenReturn(Optional.empty());
        Exception exception = assertThrows(EntityNotFoundException.class,
            () -> enrollmentRequestService.getEnrollmentRequestEntityById(TEST_ENROLLMENT_REQUEST.getId()));
        verify(enrollmentRequestRepository, times(1)).findById(TEST_ENROLLMENT_REQUEST.getId());
        assertEquals(format(ENROLLMENT_REQUEST_NOT_FOUND_ERROR_MESSAGE, TEST_ENROLLMENT_REQUEST.getId()),
            exception.getMessage());
    }

    @Test
    void testCreateEnrollmentRequest() {
        when(enrollmentRequestRepository.findEnrollmentRequestByCourseIdAndUserId(
            eq(TEST_ENROLLMENT_REQUEST.getCourse().getId()), eq(TEST_ENROLLMENT_REQUEST.getUser().getId())))
            .thenReturn(Optional.empty());
        when(courseService.getCourseEntityById(eq(TEST_ENROLLMENT_REQUEST.getCourse().getId())))
            .thenReturn(TEST_ENROLLMENT_REQUEST.getCourse());
        when(userService.getUserEntityById(eq(TEST_ENROLLMENT_REQUEST.getUser().getId())))
            .thenReturn(TEST_ENROLLMENT_REQUEST.getUser());
        when(enrollmentRequestRepository.save(any()))
            .thenReturn(TEST_ENROLLMENT_REQUEST);
        when(enrollmentRequestDtoMapper.mapToResponseDto(eq(TEST_ENROLLMENT_REQUEST)))
            .thenReturn(TEST_ENROLLMENT_REQUEST_RESPONSE_DTO);

        EnrollmentRequestResponseDto result = enrollmentRequestService.createEnrollmentRequest(
            TEST_ENROLLMENT_REQUEST.getCourse().getId(), TEST_ENROLLMENT_REQUEST.getUser().getId());

        verify(enrollmentRequestRepository, times(1))
            .findEnrollmentRequestByCourseIdAndUserId(
                eq(TEST_ENROLLMENT_REQUEST.getCourse().getId()), eq(TEST_ENROLLMENT_REQUEST.getUser().getId()));
        verify(courseService, times(1)).getCourseEntityById(eq(TEST_ENROLLMENT_REQUEST.getCourse().getId()));
        verify(userService, times(1)).getUserEntityById(eq(TEST_ENROLLMENT_REQUEST.getUser().getId()));
        verify(enrollmentRequestRepository, times(1)).save(any(EnrollmentRequest.class));
        verify(enrollmentRequestDtoMapper, times(1)).mapToResponseDto(eq(TEST_ENROLLMENT_REQUEST));
        assertEquals(TEST_ENROLLMENT_REQUEST_RESPONSE_DTO, result);
    }

    @Test
    void testCreateEnrollmentRequestWhenAlreadyExists() {
        when(enrollmentRequestRepository.findEnrollmentRequestByCourseIdAndUserId(
            eq(TEST_ENROLLMENT_REQUEST.getCourse().getId()), eq(TEST_ENROLLMENT_REQUEST.getUser().getId())))
            .thenReturn(Optional.of(TEST_ENROLLMENT_REQUEST));

        Exception exception = assertThrows(EntityAlreadyExistsException.class,
            () -> enrollmentRequestService.createEnrollmentRequest(
                TEST_ENROLLMENT_REQUEST.getCourse().getId(), TEST_ENROLLMENT_REQUEST.getUser().getId()));

        verify(enrollmentRequestRepository, times(1))
            .findEnrollmentRequestByCourseIdAndUserId(
                eq(TEST_ENROLLMENT_REQUEST.getCourse().getId()), eq(TEST_ENROLLMENT_REQUEST.getUser().getId()));
        assertEquals(format(ENROLLMENT_REQUEST_ALREADY_EXISTS_MESSAGE, TEST_ENROLLMENT_REQUEST.getCourse().getId(),
            TEST_ENROLLMENT_REQUEST.getUser().getId()), exception.getMessage());
    }

    @Test
    void testAcceptEnrollmentRequest() {
        EnrollmentRequest enrollmentRequest = new EnrollmentRequest();
        enrollmentRequest.setId(TEST_ENROLLMENT_REQUEST.getId());
        enrollmentRequest.setCourse(TEST_ENROLLMENT_REQUEST.getCourse());
        enrollmentRequest.setUser(TEST_ENROLLMENT_REQUEST.getUser());
        enrollmentRequest.setState(EnrollmentState.PENDING);

        when(enrollmentRequestRepository.findById(eq(enrollmentRequest.getId())))
            .thenReturn(Optional.of(enrollmentRequest));
        when(enrollmentService.createEnrollment(any(EnrollmentCreateDto.class)))
            .thenReturn(new EnrollmentResponseDto());
        when(enrollmentRequestRepository.save(any(EnrollmentRequest.class)))
            .thenReturn(enrollmentRequest);
        when(enrollmentRequestDtoMapper.mapToResponseDto(eq(enrollmentRequest)))
            .thenReturn(TEST_ENROLLMENT_REQUEST_RESPONSE_DTO);

        EnrollmentRequestResponseDto requestResponseDto = enrollmentRequestService
            .acceptEnrollmentRequestById(enrollmentRequest.getId());

        ArgumentCaptor<EnrollmentRequest> captor = ArgumentCaptor.forClass(EnrollmentRequest.class);
        verify(enrollmentRequestRepository, times(1)).save(captor.capture());
        assertEquals(EnrollmentState.ACCEPTED, captor.getValue().getState());
        assertEquals(TEST_ENROLLMENT_REQUEST_RESPONSE_DTO, requestResponseDto);
    }

    @Test
    void testAcceptEnrollmentRequestWhenAlreadyFinalized() {
        EnrollmentRequest enrollmentRequest = new EnrollmentRequest();
        enrollmentRequest.setId(TEST_ENROLLMENT_REQUEST.getId());
        enrollmentRequest.setState(EnrollmentState.ACCEPTED);

        when(enrollmentRequestRepository.findById(eq(enrollmentRequest.getId())))
            .thenReturn(Optional.of(enrollmentRequest));

        Exception exception = assertThrows(EnrollmentRequestAlreadyFinalizedException.class,
            () -> enrollmentRequestService.acceptEnrollmentRequestById(enrollmentRequest.getId()));

        verify(enrollmentRequestRepository, times(1))
            .findById(enrollmentRequest.getId());
        assertEquals(format(EnrollmentRequestService.ENROLLMENT_REQUEST_ALREADY_FINALIZED_MESSAGE,
            enrollmentRequest.getId()), exception.getMessage());
    }

    @Test
    void testRejectEnrollmentRequest() {
        EnrollmentRequest enrollmentRequest = new EnrollmentRequest();
        enrollmentRequest.setId(TEST_ENROLLMENT_REQUEST.getId());
        enrollmentRequest.setCourse(TEST_ENROLLMENT_REQUEST.getCourse());
        enrollmentRequest.setUser(TEST_ENROLLMENT_REQUEST.getUser());
        enrollmentRequest.setState(EnrollmentState.PENDING);

        when(enrollmentRequestRepository.findById(eq(enrollmentRequest.getId())))
            .thenReturn(Optional.of(enrollmentRequest));
        when(enrollmentRequestRepository.save(any(EnrollmentRequest.class)))
            .thenReturn(enrollmentRequest);
        when(enrollmentRequestDtoMapper.mapToResponseDto(eq(enrollmentRequest)))
            .thenReturn(TEST_ENROLLMENT_REQUEST_RESPONSE_DTO);

        EnrollmentRequestResponseDto requestResponseDto = enrollmentRequestService
            .rejectEnrollmentRequestById(enrollmentRequest.getId());

        ArgumentCaptor<EnrollmentRequest> captor = ArgumentCaptor.forClass(EnrollmentRequest.class);
        verify(enrollmentRequestRepository, times(1)).save(captor.capture());
        assertEquals(EnrollmentState.REJECTED, captor.getValue().getState());
        assertEquals(TEST_ENROLLMENT_REQUEST_RESPONSE_DTO, requestResponseDto);
    }

    @Test
    void testRejectEnrollmentRequestWhenAlreadyFinalized() {
        EnrollmentRequest enrollmentRequest = new EnrollmentRequest();
        enrollmentRequest.setId(TEST_ENROLLMENT_REQUEST.getId());
        enrollmentRequest.setState(EnrollmentState.ACCEPTED);

        when(enrollmentRequestRepository.findById(eq(enrollmentRequest.getId())))
            .thenReturn(Optional.of(enrollmentRequest));

        Exception exception = assertThrows(EnrollmentRequestAlreadyFinalizedException.class,
            () -> enrollmentRequestService.rejectEnrollmentRequestById(enrollmentRequest.getId()));

        verify(enrollmentRequestRepository, times(1))
            .findById(enrollmentRequest.getId());
        assertEquals(format(EnrollmentRequestService.ENROLLMENT_REQUEST_ALREADY_FINALIZED_MESSAGE,
            enrollmentRequest.getId()), exception.getMessage());
    }

    @Test
    void testDeleteEnrollmentRequestById() {
        when(enrollmentRequestRepository.findById(eq(TEST_ENROLLMENT_REQUEST.getId())))
            .thenReturn(Optional.of(TEST_ENROLLMENT_REQUEST));
        doNothing().when(enrollmentRequestRepository).delete(eq(TEST_ENROLLMENT_REQUEST));
        when(enrollmentRequestDtoMapper.mapToResponseDto(eq(TEST_ENROLLMENT_REQUEST)))
            .thenReturn(TEST_ENROLLMENT_REQUEST_RESPONSE_DTO);

        EnrollmentRequestResponseDto result = enrollmentRequestService.deleteEnrollmentRequestById(
            TEST_ENROLLMENT_REQUEST.getId());

        verify(enrollmentRequestRepository, times(1)).findById(TEST_ENROLLMENT_REQUEST.getId());
        verify(enrollmentRequestRepository, times(1)).delete(TEST_ENROLLMENT_REQUEST);
        verify(enrollmentRequestDtoMapper, times(1)).mapToResponseDto(eq(TEST_ENROLLMENT_REQUEST));
        assertEquals(TEST_ENROLLMENT_REQUEST_RESPONSE_DTO, result);
    }

}
