package bg.sofia.uni.fmi.webjava.backend.service;

import bg.sofia.uni.fmi.webjava.backend.mapper.AssignmentSubmissionDtoMapper;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.submission.AssignmentSubmissionCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.submission.AssignmentSubmissionGradeDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.submission.AssignmentSubmissionResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.submission.AssignmentSubmissionUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Assignment;
import bg.sofia.uni.fmi.webjava.backend.model.entity.AssignmentSubmission;
import bg.sofia.uni.fmi.webjava.backend.model.entity.User;
import bg.sofia.uni.fmi.webjava.backend.repository.AssignmentSubmissionRepository;
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
import java.util.UUID;

import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createTestAssignmentSubmission;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createTestAssignmentSubmissionResponseDto;
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
public class AssignmentSubmissionServiceTest {

    @Mock
    private AssignmentSubmissionDtoMapper assignmentSubmissionDtoMapper;

    @Mock
    private AssignmentSubmissionRepository assignmentSubmissionRepository;

    @Mock
    private UserService userService;

    @Mock
    private AssignmentService assignmentService;

    @InjectMocks
    private AssignmentSubmissionService assignmentSubmissionService;

    private static final AssignmentSubmission TEST_SUBMISSION = createTestAssignmentSubmission();
    private static final AssignmentSubmissionResponseDto
        TEST_SUBMISSION_RESPONSE_DTO = createTestAssignmentSubmissionResponseDto();

    @Test
    void testGetSubmissionsByAssignmentId() {
        when(assignmentSubmissionRepository.findAssignmentSubmissionsByAssignmentId(
            eq(TEST_SUBMISSION.getAssignment().getId()), any(Pageable.class)
        )).thenReturn((new PageImpl<>(List.of(TEST_SUBMISSION))));

        when(assignmentSubmissionDtoMapper.mapToResponseDto(eq(TEST_SUBMISSION)))
            .thenReturn(TEST_SUBMISSION_RESPONSE_DTO);

        Page<AssignmentSubmissionResponseDto> result = assignmentSubmissionService
            .getSubmissionsByAssignmentId(TEST_SUBMISSION.getAssignment().getId(),
                PageRequest.of(0, 10));

        verify(assignmentSubmissionRepository, times(1))
            .findAssignmentSubmissionsByAssignmentId(
                eq(TEST_SUBMISSION.getAssignment().getId()), any(Pageable.class));
        assertNotNull(result);
        assertTrue(result.getContent().contains(TEST_SUBMISSION_RESPONSE_DTO));
    }

    @Test
    void testGetAssignmentSubmissionById() {
        when(assignmentSubmissionRepository.findById(eq(TEST_SUBMISSION.getId())))
            .thenReturn(Optional.of(TEST_SUBMISSION));
        when(assignmentSubmissionDtoMapper.mapToResponseDto(eq(TEST_SUBMISSION)))
            .thenReturn(TEST_SUBMISSION_RESPONSE_DTO);

        AssignmentSubmissionResponseDto result = assignmentSubmissionService
            .getAssignmentSubmissionById(TEST_SUBMISSION.getId());

        verify(assignmentSubmissionRepository, times(1)).findById(eq(TEST_SUBMISSION.getId()));
        verify(assignmentSubmissionDtoMapper, times(1)).mapToResponseDto(eq(TEST_SUBMISSION));
        assertEquals(TEST_SUBMISSION_RESPONSE_DTO, result);
    }

    @Test
    void testCreateAssignmentSubmission() {
        AssignmentSubmissionCreateDto createDto = new AssignmentSubmissionCreateDto();
        createDto.setUserId(UUID.randomUUID());
        Assignment assignment = TEST_SUBMISSION.getAssignment();
        User user = TEST_SUBMISSION.getUser();

        when(assignmentSubmissionDtoMapper.mapDtoToAssignmentSubmission(eq(createDto)))
            .thenReturn(TEST_SUBMISSION);
        when(assignmentService.getAssignmentEntityById(eq(assignment.getId())))
            .thenReturn(assignment);
        when(userService.getUserEntityById(eq(createDto.getUserId())))
            .thenReturn(user);
        when(assignmentSubmissionRepository.save(eq(TEST_SUBMISSION)))
            .thenReturn(TEST_SUBMISSION);
        when(assignmentSubmissionDtoMapper.mapToResponseDto(eq(TEST_SUBMISSION)))
            .thenReturn(TEST_SUBMISSION_RESPONSE_DTO);

        AssignmentSubmissionResponseDto result = assignmentSubmissionService
            .createAssignmentSubmission(assignment.getId(), createDto);

        verify(assignmentSubmissionDtoMapper, times(1)).mapDtoToAssignmentSubmission(eq(createDto));
        verify(assignmentService, times(1)).getAssignmentEntityById(eq(assignment.getId()));
        verify(userService, times(1)).getUserEntityById(eq(createDto.getUserId()));
        verify(assignmentSubmissionRepository, times(1)).save(eq(TEST_SUBMISSION));
        verify(assignmentSubmissionDtoMapper, times(1)).mapToResponseDto(eq(TEST_SUBMISSION));
        assertEquals(TEST_SUBMISSION_RESPONSE_DTO, result);
    }

    @Test
    void testDeleteAssignmentSubmissionById() {
        when(assignmentSubmissionRepository.findById(eq(TEST_SUBMISSION.getId())))
            .thenReturn(Optional.of(TEST_SUBMISSION));
        when(assignmentSubmissionDtoMapper.mapToResponseDto(eq(TEST_SUBMISSION)))
            .thenReturn(TEST_SUBMISSION_RESPONSE_DTO);

        AssignmentSubmissionResponseDto result = assignmentSubmissionService
            .deleteAssignmentSubmissionById(TEST_SUBMISSION.getId());

        verify(assignmentSubmissionRepository, times(1)).findById(eq(TEST_SUBMISSION.getId()));
        verify(assignmentSubmissionDtoMapper, times(1)).mapToResponseDto(eq(TEST_SUBMISSION));
        verify(assignmentSubmissionRepository, times(1)).deleteById(eq(TEST_SUBMISSION.getId()));
        assertEquals(TEST_SUBMISSION_RESPONSE_DTO, result);
    }

    @Test
    void testUpdateAssignmentSubmissionById() {
        AssignmentSubmissionUpdateDto updateDto = new AssignmentSubmissionUpdateDto();
        when(assignmentSubmissionRepository.findById(eq(TEST_SUBMISSION.getId())))
            .thenReturn(Optional.of(TEST_SUBMISSION));
        doNothing().when(assignmentSubmissionDtoMapper)
                   .updateAssignmentSubmissionFromDto(eq(updateDto), eq(TEST_SUBMISSION));
        when(assignmentSubmissionRepository.save(eq(TEST_SUBMISSION)))
            .thenReturn(TEST_SUBMISSION);
        when(assignmentSubmissionDtoMapper.mapToResponseDto(eq(TEST_SUBMISSION)))
            .thenReturn(TEST_SUBMISSION_RESPONSE_DTO);

        AssignmentSubmissionResponseDto result = assignmentSubmissionService
            .updateAssignmentSubmissionById(TEST_SUBMISSION.getId(), updateDto);

        verify(assignmentSubmissionRepository, times(1)).findById(eq(TEST_SUBMISSION.getId()));
        verify(assignmentSubmissionDtoMapper, times(1))
            .updateAssignmentSubmissionFromDto(eq(updateDto), eq(TEST_SUBMISSION));
        verify(assignmentSubmissionRepository, times(1)).save(eq(TEST_SUBMISSION));
        verify(assignmentSubmissionDtoMapper, times(1)).mapToResponseDto(eq(TEST_SUBMISSION));
        assertEquals(TEST_SUBMISSION_RESPONSE_DTO, result);
    }

    @Test
    void testGradeAssignmentSubmissionById() {
        AssignmentSubmissionGradeDto gradeDto = new AssignmentSubmissionGradeDto();
        gradeDto.setGrade(TEST_SUBMISSION.getAssignment().getTotalPoints() + 10); // Exceeds total points

        when(assignmentSubmissionRepository.findById(eq(TEST_SUBMISSION.getId())))
            .thenReturn(Optional.of(TEST_SUBMISSION));
        doNothing().when(assignmentSubmissionDtoMapper)
                   .gradeAssignmentSubmissionFromDto(any(), eq(TEST_SUBMISSION));
        when(assignmentSubmissionRepository.save(eq(TEST_SUBMISSION)))
            .thenReturn(TEST_SUBMISSION);
        when(assignmentSubmissionDtoMapper.mapToResponseDto(eq(TEST_SUBMISSION)))
            .thenReturn(TEST_SUBMISSION_RESPONSE_DTO);

        AssignmentSubmissionResponseDto result = assignmentSubmissionService
            .gradeAssignmentSubmissionById(TEST_SUBMISSION.getId(), gradeDto);

        verify(assignmentSubmissionRepository, times(1)).findById(eq(TEST_SUBMISSION.getId()));
        verify(assignmentSubmissionDtoMapper, times(1))
            .gradeAssignmentSubmissionFromDto(any(), eq(TEST_SUBMISSION));
        verify(assignmentSubmissionRepository, times(1)).save(eq(TEST_SUBMISSION));
        verify(assignmentSubmissionDtoMapper, times(1)).mapToResponseDto(eq(TEST_SUBMISSION));
        assertEquals(TEST_SUBMISSION_RESPONSE_DTO, result);
        assertEquals(TEST_SUBMISSION.getAssignment().getTotalPoints(), gradeDto.getGrade());
    }

    @Test
    void testGetAssignmentSubmissionByIdThrowsForMissing() {
        UUID missingId = UUID.randomUUID();
        when(assignmentSubmissionRepository.findById(eq(missingId))).thenReturn(Optional.empty());
        Exception ex = assertThrows(IllegalArgumentException.class,
            () -> assignmentSubmissionService.getAssignmentSubmissionById(missingId));
        assertTrue(ex.getMessage().contains(missingId.toString()));
    }

}
