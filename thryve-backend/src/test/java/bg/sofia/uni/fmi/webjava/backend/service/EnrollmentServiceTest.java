package bg.sofia.uni.fmi.webjava.backend.service;

import bg.sofia.uni.fmi.webjava.backend.exception.EntityAlreadyExistsException;
import bg.sofia.uni.fmi.webjava.backend.exception.EntityNotFoundException;
import bg.sofia.uni.fmi.webjava.backend.mapper.EnrollmentDtoMapper;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.EnrollmentCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.EnrollmentResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.EnrollmentUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Enrollment;
import bg.sofia.uni.fmi.webjava.backend.repository.EnrollmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static bg.sofia.uni.fmi.webjava.backend.service.EnrollmentService.ENROLLMENT_ALREADY_EXISTS_ERROR_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.service.EnrollmentService.ENROLLMENT_NOT_FOUND_ERROR_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createTestEnrollment;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createTestEnrollmentResponseDto;
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
public class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private EnrollmentDtoMapper enrollmentDtoMapper;

    @Mock
    private CourseService courseService;

    @Mock
    private UserService userService;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private static final Enrollment TEST_ENROLLMENT = createTestEnrollment();
    private static final EnrollmentResponseDto TEST_ENROLLMENT_RESPONSE_DTO = createTestEnrollmentResponseDto();

    @Test
    void testGetAllEnrollments() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Enrollment> expectedEnrollments = List.of(TEST_ENROLLMENT);
        Page<Enrollment> page = new PageImpl<>(expectedEnrollments);

        when(enrollmentRepository.findAll(eq(pageable))).thenReturn(page);
        when(enrollmentDtoMapper.mapToResponseDto(eq(TEST_ENROLLMENT))).thenReturn(TEST_ENROLLMENT_RESPONSE_DTO);

        Page<EnrollmentResponseDto> result = enrollmentService.getAllEnrollments(pageable);

        verify(enrollmentRepository, times(1)).findAll(eq(pageable));
        assertNotNull(result);
        assertEquals(expectedEnrollments.size(), result.getContent().size());
        assertTrue(result.getContent().contains(TEST_ENROLLMENT_RESPONSE_DTO));
    }

    @Test
    void testGetEnrollmentsByCourseIdAndUserId() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Enrollment> expectedEnrollments = List.of(TEST_ENROLLMENT);
        Page<Enrollment> page = new PageImpl<>(expectedEnrollments);

        when(enrollmentRepository.findEnrollmentByCourseIdAndUserId(
            eq(TEST_ENROLLMENT.getCourse().getId()), eq(TEST_ENROLLMENT.getUser().getId()), eq(pageable)))
            .thenReturn(page);
        when(enrollmentDtoMapper.mapToResponseDto(eq(TEST_ENROLLMENT)))
            .thenReturn(TEST_ENROLLMENT_RESPONSE_DTO);

        Page<EnrollmentResponseDto> result = enrollmentService
            .getEnrollmentsByCourseIdAndUserId(TEST_ENROLLMENT.getCourse().getId(),
                TEST_ENROLLMENT_RESPONSE_DTO.getUser().getId(), pageable);

        verify(enrollmentRepository, times(1))
            .findEnrollmentByCourseIdAndUserId(eq(TEST_ENROLLMENT.getCourse().getId()),
                eq(TEST_ENROLLMENT.getUser().getId()), eq(pageable));
        assertNotNull(result);
        assertEquals(expectedEnrollments.size(), result.getContent().size());
        assertTrue(result.getContent().contains(TEST_ENROLLMENT_RESPONSE_DTO));
    }

    @Test
    void testGetEnrollmentsByUserId() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Enrollment> expectedEnrollments = List.of(TEST_ENROLLMENT);
        Page<Enrollment> page = new PageImpl<>(expectedEnrollments);

        when(enrollmentRepository.findEnrollmentsByUserId(
            eq(TEST_ENROLLMENT.getUser().getId()), eq(pageable)))
            .thenReturn(page);
        when(enrollmentDtoMapper.mapToResponseDto(eq(TEST_ENROLLMENT)))
            .thenReturn(TEST_ENROLLMENT_RESPONSE_DTO);

        Page<EnrollmentResponseDto> result = enrollmentService
            .getEnrollmentsByUserId(TEST_ENROLLMENT_RESPONSE_DTO.getUser().getId(), pageable);

        verify(enrollmentRepository, times(1))
            .findEnrollmentsByUserId(eq(TEST_ENROLLMENT.getUser().getId()), eq(pageable));
        assertNotNull(result);
        assertEquals(expectedEnrollments.size(), result.getContent().size());
        assertTrue(result.getContent().contains(TEST_ENROLLMENT_RESPONSE_DTO));
    }

    @Test
    void testGetEnrollmentsByCourseId() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Enrollment> expectedEnrollments = List.of(TEST_ENROLLMENT);
        Page<Enrollment> page = new PageImpl<>(expectedEnrollments);

        when(enrollmentRepository.findEnrollmentsByCourseId(
            eq(TEST_ENROLLMENT.getCourse().getId()), eq(pageable)))
            .thenReturn(page);
        when(enrollmentDtoMapper.mapToResponseDto(eq(TEST_ENROLLMENT)))
            .thenReturn(TEST_ENROLLMENT_RESPONSE_DTO);

        Page<EnrollmentResponseDto> result = enrollmentService
            .getEnrollmentsByCourseId(TEST_ENROLLMENT_RESPONSE_DTO.getCourse().getId(), pageable);

        verify(enrollmentRepository, times(1))
            .findEnrollmentsByCourseId(eq(TEST_ENROLLMENT.getCourse().getId()), eq(pageable));
        assertNotNull(result);
        assertEquals(expectedEnrollments.size(), result.getContent().size());
        assertTrue(result.getContent().contains(TEST_ENROLLMENT_RESPONSE_DTO));
    }

    @Test
    void testGetEnrollmentEntityById() {
        when(enrollmentRepository.findById(eq(TEST_ENROLLMENT.getId())))
            .thenReturn(Optional.of(TEST_ENROLLMENT));
        Enrollment result = enrollmentService.getEnrollmentEntityById(TEST_ENROLLMENT.getId());
        verify(enrollmentRepository, times(1)).findById(TEST_ENROLLMENT.getId());
        assertEquals(TEST_ENROLLMENT, result);
    }

    @Test
    void testGetEnrollmentEntityByIdForNonExistingId() {
        when(enrollmentRepository.findById(eq(TEST_ENROLLMENT.getId())))
            .thenReturn(Optional.empty());
        Exception exception = assertThrows(EntityNotFoundException.class,
            () -> enrollmentService.getEnrollmentEntityById(TEST_ENROLLMENT.getId()));
        verify(enrollmentRepository, times(1)).findById(TEST_ENROLLMENT.getId());
        assertEquals(format(ENROLLMENT_NOT_FOUND_ERROR_MESSAGE, TEST_ENROLLMENT.getId()),
            exception.getMessage());
    }

    @Test
    void testGetEnrollmentById() {
        when(enrollmentRepository.findById(eq(TEST_ENROLLMENT.getId())))
            .thenReturn(Optional.of(TEST_ENROLLMENT));
        when(enrollmentDtoMapper.mapToResponseDto(eq(TEST_ENROLLMENT)))
            .thenReturn(TEST_ENROLLMENT_RESPONSE_DTO);

        EnrollmentResponseDto result = enrollmentService.getEnrollmentById(TEST_ENROLLMENT.getId());

        verify(enrollmentRepository, times(1)).findById(TEST_ENROLLMENT.getId());
        verify(enrollmentDtoMapper, times(1)).mapToResponseDto(TEST_ENROLLMENT);
        assertEquals(TEST_ENROLLMENT_RESPONSE_DTO, result);
    }

    @Test
    void testGetEnrollmentByIdForNonExistingId() {
        when(enrollmentRepository.findById(eq(TEST_ENROLLMENT.getId())))
            .thenReturn(Optional.empty());
        Exception exception = assertThrows(EntityNotFoundException.class,
            () -> enrollmentService.getEnrollmentById(TEST_ENROLLMENT.getId()));
        verify(enrollmentRepository, times(1)).findById(TEST_ENROLLMENT.getId());
        assertEquals(format(ENROLLMENT_NOT_FOUND_ERROR_MESSAGE, TEST_ENROLLMENT.getId()),
            exception.getMessage());
    }

    @Test
    void testCreateEnrollment() {
        EnrollmentCreateDto enrollmentCreateDto = new EnrollmentCreateDto();
        enrollmentCreateDto.setUserId(TEST_ENROLLMENT.getUser().getId());
        enrollmentCreateDto.setCourseId(TEST_ENROLLMENT.getCourse().getId());
        when(enrollmentDtoMapper.mapDtoToEnrollment(any(EnrollmentCreateDto.class)))
            .thenReturn(TEST_ENROLLMENT);
        when(enrollmentRepository.findEnrollmentByCourseIdAndUserId(
            any(), any(), any(Pageable.class)))
            .thenReturn(Page.empty());
        when(courseService.getCourseEntityById(TEST_ENROLLMENT.getCourse().getId()))
            .thenReturn(TEST_ENROLLMENT.getCourse());
        when(userService.getUserEntityById(TEST_ENROLLMENT.getUser().getId()))
            .thenReturn(TEST_ENROLLMENT.getUser());
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(TEST_ENROLLMENT);
        when(enrollmentDtoMapper.mapToResponseDto(eq(TEST_ENROLLMENT)))
            .thenReturn(TEST_ENROLLMENT_RESPONSE_DTO);

        EnrollmentResponseDto result = enrollmentService.createEnrollment(enrollmentCreateDto);
        assertEquals(TEST_ENROLLMENT_RESPONSE_DTO, result);
    }

    @Test
    void testCreateEnrollmentForAlreadyExistingEnrollment() {
        EnrollmentCreateDto enrollmentCreateDto = new EnrollmentCreateDto();
        enrollmentCreateDto.setUserId(TEST_ENROLLMENT.getUser().getId());
        enrollmentCreateDto.setCourseId(TEST_ENROLLMENT.getCourse().getId());

        when(enrollmentDtoMapper.mapDtoToEnrollment(eq(enrollmentCreateDto)))
            .thenReturn(TEST_ENROLLMENT);
        when(enrollmentRepository.findEnrollmentByCourseIdAndUserId(
            any(), any(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(TEST_ENROLLMENT)));

        Exception exception = assertThrows(EntityAlreadyExistsException.class,
            () -> enrollmentService.createEnrollment(enrollmentCreateDto));

        assertEquals(ENROLLMENT_ALREADY_EXISTS_ERROR_MESSAGE,
            exception.getMessage());
    }

    @Test
    void testUpdateEnrollmentById() {
        when(enrollmentRepository.findById(eq(TEST_ENROLLMENT.getId())))
            .thenReturn(Optional.of(TEST_ENROLLMENT));
        doNothing().when(enrollmentDtoMapper)
                   .updateEnrollmentFromDto(any(EnrollmentUpdateDto.class), eq(TEST_ENROLLMENT));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(TEST_ENROLLMENT);
        when(enrollmentDtoMapper.mapToResponseDto(eq(TEST_ENROLLMENT)))
            .thenReturn(TEST_ENROLLMENT_RESPONSE_DTO);

        EnrollmentResponseDto result = enrollmentService
            .updateEnrollmentById(TEST_ENROLLMENT.getId(), new EnrollmentUpdateDto());

        verify(enrollmentRepository, times(1)).findById(TEST_ENROLLMENT.getId());
        verify(enrollmentDtoMapper, times(1))
            .updateEnrollmentFromDto(any(EnrollmentUpdateDto.class), eq(TEST_ENROLLMENT));
        verify(enrollmentRepository, times(1)).save(TEST_ENROLLMENT);
        verify(enrollmentDtoMapper, times(1)).mapToResponseDto(TEST_ENROLLMENT);
        assertEquals(TEST_ENROLLMENT_RESPONSE_DTO, result);
    }

    @Test
    void testDeleteEnrollmentById() {
        when(enrollmentRepository.findById(eq(TEST_ENROLLMENT.getId())))
            .thenReturn(Optional.of(TEST_ENROLLMENT));
        when(enrollmentDtoMapper.mapToResponseDto(eq(TEST_ENROLLMENT)))
            .thenReturn(TEST_ENROLLMENT_RESPONSE_DTO);

        doNothing().when(enrollmentRepository).deleteById(TEST_ENROLLMENT.getId());

        EnrollmentResponseDto result = enrollmentService.deleteEnrollmentById(TEST_ENROLLMENT.getId());
        assertEquals(TEST_ENROLLMENT_RESPONSE_DTO, result);
    }

}
