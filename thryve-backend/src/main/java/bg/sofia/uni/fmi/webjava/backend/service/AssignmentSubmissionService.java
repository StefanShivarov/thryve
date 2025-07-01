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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class AssignmentSubmissionService {

    private static final String SUBMISSION_NOT_FOUND_MESSAGE = "Submission with id %s not found!";

    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final AssignmentSubmissionDtoMapper assignmentSubmissionDtoMapper;
    private final UserService userService;
    private final AssignmentService assignmentService;

    @Transactional
    public Page<AssignmentSubmissionResponseDto> getSubmissionsByAssignmentId(UUID assignmentId, Pageable pageable) {
        return assignmentSubmissionRepository.findAssignmentSubmissionsByAssignmentId(assignmentId, pageable)
            .map(assignmentSubmissionDtoMapper::mapToResponseDto);
    }

    @Transactional
    public AssignmentSubmissionResponseDto getAssignmentSubmissionById(UUID id) {
        return assignmentSubmissionDtoMapper.mapToResponseDto(getAssignmentSubmissionEntityById(id));
    }

    @Transactional
    protected AssignmentSubmission getAssignmentSubmissionEntityById(UUID id) {
        return assignmentSubmissionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(format(SUBMISSION_NOT_FOUND_MESSAGE, id)));
    }

    @Transactional
    public AssignmentSubmissionResponseDto createAssignmentSubmission(UUID assignmentId,
                                                                      AssignmentSubmissionCreateDto submissionCreateDto) {
        AssignmentSubmission submission = assignmentSubmissionDtoMapper
            .mapDtoToAssignmentSubmission(submissionCreateDto);

        Assignment assignment = assignmentService.getAssignmentEntityById(assignmentId);
        submission.setAssignment(assignment);

        User user = userService.getUserEntityById(submissionCreateDto.getUserId());
        submission.setUser(user);

        AssignmentSubmission savedSubmission = assignmentSubmissionRepository.save(submission);
        return assignmentSubmissionDtoMapper.mapToResponseDto(savedSubmission);
    }

    @Transactional
    public AssignmentSubmissionResponseDto deleteAssignmentSubmissionById(UUID id) {
        AssignmentSubmissionResponseDto response = getAssignmentSubmissionById(id);
        assignmentSubmissionRepository.deleteById(id);
        return response;
    }

    @Transactional
    public AssignmentSubmissionResponseDto updateAssignmentSubmissionById(UUID id,
                                                                          AssignmentSubmissionUpdateDto submissionUpdateDto) {
        AssignmentSubmission submission = getAssignmentSubmissionEntityById(id);
        assignmentSubmissionDtoMapper.updateAssignmentSubmissionFromDto(submissionUpdateDto, submission);
        return assignmentSubmissionDtoMapper.mapToResponseDto(assignmentSubmissionRepository.save(submission));
    }

    @Transactional
    public AssignmentSubmissionResponseDto gradeAssignmentSubmissionById(UUID id,
                                                                         AssignmentSubmissionGradeDto submissionGradeDto) {
        AssignmentSubmission submission = getAssignmentSubmissionEntityById(id);
        double totalPoints = submission.getAssignment().getTotalPoints();
        if (submissionGradeDto.getGrade() > totalPoints) {
            submissionGradeDto.setGrade(totalPoints);
        }
        assignmentSubmissionDtoMapper.gradeAssignmentSubmissionFromDto(submissionGradeDto, submission);
        return assignmentSubmissionDtoMapper.mapToResponseDto(assignmentSubmissionRepository.save(submission));
    }

}
