package bg.sofia.uni.fmi.webjava.backend.service;

import bg.sofia.uni.fmi.webjava.backend.exception.EntityNotFoundException;
import bg.sofia.uni.fmi.webjava.backend.mapper.AssignmentDtoMapper;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.AssignmentCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.AssignmentResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.AssignmentUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Assignment;
import bg.sofia.uni.fmi.webjava.backend.repository.AssignmentRepository;
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

import static bg.sofia.uni.fmi.webjava.backend.service.AssignmentService.ASSIGNMENT_NOT_FOUND_ERROR_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createTestAssignment;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createTestAssignmentResponseDto;
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
public class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private AssignmentDtoMapper assignmentDtoMapper;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private AssignmentService assignmentService;

    private static final Assignment TEST_ASSIGNMENT = createTestAssignment();
    private static final AssignmentResponseDto
        TEST_ASSIGNMENT_RESPONSE_DTO = createTestAssignmentResponseDto();

    @Test
    void testGetAssignmentsByCourseId() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Assignment> expectedAssignments = List.of(TEST_ASSIGNMENT);
        Page<Assignment> page = new PageImpl<>(expectedAssignments);

        when(assignmentRepository.findAssignmentsByCourseId(any(), eq(pageable)))
            .thenReturn(page);
        when(assignmentDtoMapper.mapToResponseDto(eq(TEST_ASSIGNMENT)))
            .thenReturn(TEST_ASSIGNMENT_RESPONSE_DTO);

        Page<AssignmentResponseDto> result = assignmentService
            .getAssignmentsByCourseId(TEST_ASSIGNMENT.getCourse().getId(), pageable);

        assertNotNull(result);
        assertEquals(expectedAssignments.size(), result.getContent().size());
        assertTrue(result.getContent().contains(TEST_ASSIGNMENT_RESPONSE_DTO));
    }

    @Test
    void testGetAssignmentEntityById() {
        when(assignmentRepository.findById(eq(TEST_ASSIGNMENT.getId()))).thenReturn(Optional.of(TEST_ASSIGNMENT));
        Assignment result = assignmentService.getAssignmentEntityById(TEST_ASSIGNMENT.getId());
        verify(assignmentRepository, times(1)).findById(eq(TEST_ASSIGNMENT.getId()));
        assertEquals(TEST_ASSIGNMENT, result);
    }

    @Test
    void testGetAssignmentEntityByIdForNonExistingAssignmentId() {
        when(assignmentRepository.findById(eq(TEST_ASSIGNMENT.getId()))).thenReturn(Optional.empty());
        Exception exception = assertThrows(EntityNotFoundException.class,
            () -> assignmentService.getAssignmentEntityById(TEST_ASSIGNMENT.getId()));
        assertEquals(format(ASSIGNMENT_NOT_FOUND_ERROR_MESSAGE, TEST_ASSIGNMENT.getId()), exception.getMessage());
    }

    @Test
    void testCreateAssignment() {
        when(assignmentDtoMapper.mapDtoToAssignment(any(AssignmentCreateDto.class)))
            .thenReturn(TEST_ASSIGNMENT);
        when(courseService.getCourseEntityById(eq(TEST_ASSIGNMENT.getCourse().getId())))
            .thenReturn(TEST_ASSIGNMENT.getCourse());
        when(assignmentRepository.save(eq(TEST_ASSIGNMENT))).thenReturn(TEST_ASSIGNMENT);
        when(assignmentDtoMapper.mapToResponseDto(eq(TEST_ASSIGNMENT))).thenReturn(TEST_ASSIGNMENT_RESPONSE_DTO);

        AssignmentResponseDto result = assignmentService.createAssignment(TEST_ASSIGNMENT.getCourse().getId(),
            new AssignmentCreateDto());

        assertEquals(TEST_ASSIGNMENT_RESPONSE_DTO, result);
    }

    @Test
    void testUpdateAssignmentById() {
        when(assignmentRepository.findById(eq(TEST_ASSIGNMENT.getId())))
            .thenReturn(Optional.of(TEST_ASSIGNMENT));
        doNothing().when(assignmentDtoMapper)
                   .updateAssignmentFromDto(any(AssignmentUpdateDto.class), eq(TEST_ASSIGNMENT));
        when(assignmentRepository.save(eq(TEST_ASSIGNMENT))).thenReturn(TEST_ASSIGNMENT);
        when(assignmentDtoMapper.mapToResponseDto(eq(TEST_ASSIGNMENT)))
            .thenReturn(TEST_ASSIGNMENT_RESPONSE_DTO);

        AssignmentResponseDto result = assignmentService
            .updateAssignmentById(TEST_ASSIGNMENT.getId(), new AssignmentUpdateDto());

        verify(assignmentRepository, times(1)).findById(eq(TEST_ASSIGNMENT.getId()));
        verify(assignmentDtoMapper, times(1))
            .updateAssignmentFromDto(any(AssignmentUpdateDto.class), eq(TEST_ASSIGNMENT));
        verify(assignmentRepository, times(1)).save(eq(TEST_ASSIGNMENT));
        verify(assignmentDtoMapper, times(1)).mapToResponseDto(eq(TEST_ASSIGNMENT));
        assertEquals(TEST_ASSIGNMENT_RESPONSE_DTO, result);
    }

    @Test
    void testDeleteAssignmentById() {
        when(assignmentRepository.findById(eq(TEST_ASSIGNMENT.getId())))
            .thenReturn(Optional.of(TEST_ASSIGNMENT));
        when(assignmentDtoMapper.mapToResponseDto(eq(TEST_ASSIGNMENT)))
            .thenReturn(TEST_ASSIGNMENT_RESPONSE_DTO);
        doNothing().when(assignmentRepository).delete(eq(TEST_ASSIGNMENT));

        AssignmentResponseDto result = assignmentService.deleteAssignmentById(TEST_ASSIGNMENT.getId());

        verify(assignmentRepository, times(1)).findById(eq(TEST_ASSIGNMENT.getId()));
        verify(assignmentDtoMapper, times(1)).mapToResponseDto(eq(TEST_ASSIGNMENT));
        verify(assignmentRepository, times(1)).delete(eq(TEST_ASSIGNMENT));
        assertEquals(TEST_ASSIGNMENT_RESPONSE_DTO, result);
    }

}
