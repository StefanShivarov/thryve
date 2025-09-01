package bg.sofia.uni.fmi.webjava.backend.service;

import bg.sofia.uni.fmi.webjava.backend.exception.EntityNotFoundException;
import bg.sofia.uni.fmi.webjava.backend.mapper.AssignmentDtoMapper;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.AssignmentCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.AssignmentResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.AssignmentUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Assignment;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Course;
import bg.sofia.uni.fmi.webjava.backend.repository.AssignmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    public static final String ASSIGNMENT_NOT_FOUND_ERROR_MESSAGE = "Assignment with id %s was not found!";

    private final AssignmentRepository assignmentRepository;
    private final AssignmentDtoMapper assignmentDtoMapper;
    private final CourseService courseService;

    @Transactional
    public Page<AssignmentResponseDto> getAssignmentsByCourseId(UUID courseId, Pageable pageable) {
        return assignmentRepository.findAssignmentsByCourseId(courseId, pageable)
            .map(assignmentDtoMapper::mapToResponseDto);
    }

    @Transactional
    public AssignmentResponseDto createAssignment(UUID courseId, AssignmentCreateDto assignmentCreateDto) {
        Course course = courseService.getCourseEntityById(courseId);
        Assignment assignment = assignmentDtoMapper.mapDtoToAssignment(assignmentCreateDto);
        assignment.setCourse(course);
        return assignmentDtoMapper.mapToResponseDto(assignmentRepository.save(assignment));
    }

    @Transactional
    protected Assignment getAssignmentEntityById(UUID id) {
        return assignmentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(format(ASSIGNMENT_NOT_FOUND_ERROR_MESSAGE, id)));
    }

    @Transactional
    public AssignmentResponseDto updateAssignmentById(UUID id, AssignmentUpdateDto assignmentUpdateDto) {
        Assignment assignment = getAssignmentEntityById(id);
        assignmentDtoMapper.updateAssignmentFromDto(assignmentUpdateDto, assignment);
        return assignmentDtoMapper.mapToResponseDto(assignmentRepository.save(assignment));
    }

    @Transactional
    public AssignmentResponseDto deleteAssignmentById(UUID id) {
        Assignment assignment = getAssignmentEntityById(id);
        assignmentRepository.delete(assignment);
        return assignmentDtoMapper.mapToResponseDto(assignment);
    }

}
