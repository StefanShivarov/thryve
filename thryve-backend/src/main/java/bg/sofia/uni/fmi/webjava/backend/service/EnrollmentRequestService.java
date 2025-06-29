package bg.sofia.uni.fmi.webjava.backend.service;

import bg.sofia.uni.fmi.webjava.backend.exception.EnrollmentRequestAlreadyFinalizedException;
import bg.sofia.uni.fmi.webjava.backend.exception.EntityAlreadyExistsException;
import bg.sofia.uni.fmi.webjava.backend.exception.EntityNotFoundException;
import bg.sofia.uni.fmi.webjava.backend.mapper.EnrollmentRequestDtoMapper;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.EnrollmentCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.request.EnrollmentRequestResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Course;
import bg.sofia.uni.fmi.webjava.backend.model.entity.EnrollmentRequest;
import bg.sofia.uni.fmi.webjava.backend.model.entity.EnrollmentState;
import bg.sofia.uni.fmi.webjava.backend.model.entity.User;
import bg.sofia.uni.fmi.webjava.backend.repository.EnrollmentRequestRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class EnrollmentRequestService {

    public static final String ENROLLMENT_REQUEST_NOT_FOUND_ERROR_MESSAGE = "Enrollment request with id %s was not found!";
    public static final String ENROLLMENT_REQUEST_ALREADY_EXISTS_MESSAGE =
        "Enrollment request for course with id %s and user with id %s not found!";
    public static final String ENROLLMENT_REQUEST_ALREADY_FINALIZED_MESSAGE =
        "Enrollment request with id %s is already in a final state!";

    private final EnrollmentRequestRepository enrollmentRequestRepository;
    private final EnrollmentRequestDtoMapper enrollmentRequestDtoMapper;
    private final CourseService courseService;
    private final UserService userService;
    private final EnrollmentService enrollmentService;

    @Transactional
    public Page<EnrollmentRequestResponseDto> getEnrollmentsByCourseId(UUID courseId, Pageable pageable) {
        return enrollmentRequestRepository.findEnrollmentRequestsByCourseId(courseId, pageable)
            .map(enrollmentRequestDtoMapper::mapToResponseDto);
    }

    @Transactional
    public Page<EnrollmentRequestResponseDto> getEnrollmentsByUserId(UUID userId, Pageable pageable) {
        return enrollmentRequestRepository.findEnrollmentRequestsByUserId(userId, pageable)
            .map(enrollmentRequestDtoMapper::mapToResponseDto);
    }

    @Transactional
    protected EnrollmentRequest getEnrollmentRequestEntityById(UUID id) {
        return enrollmentRequestRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(format(ENROLLMENT_REQUEST_NOT_FOUND_ERROR_MESSAGE, id)));
    }

    @Transactional
    public EnrollmentRequestResponseDto createEnrollmentRequest(UUID courseId, UUID userId) {
        if (enrollmentRequestRepository.findEnrollmentRequestByCourseIdAndUserId(courseId, userId).isPresent()) {
            throw new EntityAlreadyExistsException(format(ENROLLMENT_REQUEST_ALREADY_EXISTS_MESSAGE, courseId, userId));
        }

        Course course = courseService.getCourseEntityById(courseId);
        User user = userService.getUserEntityById(userId);

        EnrollmentRequest request = new EnrollmentRequest();
        request.setCourse(course);
        request.setUser(user);

        return enrollmentRequestDtoMapper.mapToResponseDto(enrollmentRequestRepository.save(request));
    }

    @Transactional
    public EnrollmentRequestResponseDto acceptEnrollmentRequestById(UUID id) {
        System.out.println("here");
        EnrollmentRequest request = getEnrollmentRequestEntityById(id);
        if (isEnrollmentRequestInFinalState(request)) {
            throw new EnrollmentRequestAlreadyFinalizedException(
                format(ENROLLMENT_REQUEST_ALREADY_FINALIZED_MESSAGE, request.getId()));
        }
        request.setState(EnrollmentState.ACCEPTED);

        EnrollmentCreateDto enrollmentCreateDto = new EnrollmentCreateDto();
        enrollmentCreateDto.setCourseId(request.getCourse().getId());
        enrollmentCreateDto.setUserId(request.getUser().getId());
        enrollmentService.createEnrollment(enrollmentCreateDto);

        return enrollmentRequestDtoMapper.mapToResponseDto(enrollmentRequestRepository.save(request));
    }

    @Transactional
    public EnrollmentRequestResponseDto rejectEnrollmentRequestById(UUID id) {
        EnrollmentRequest request = getEnrollmentRequestEntityById(id);
        if (isEnrollmentRequestInFinalState(request)) {
            throw new EnrollmentRequestAlreadyFinalizedException(
                format(ENROLLMENT_REQUEST_ALREADY_FINALIZED_MESSAGE, request.getId()));
        }
        request.setState(EnrollmentState.REJECTED);
        return enrollmentRequestDtoMapper.mapToResponseDto(enrollmentRequestRepository.save(request));
    }

    private boolean isEnrollmentRequestInFinalState(EnrollmentRequest request) {
        return request.getState() != EnrollmentState.PENDING;
    }

    @Transactional
    public EnrollmentRequestResponseDto deleteEnrollmentRequestById(UUID id) {
        EnrollmentRequest request = getEnrollmentRequestEntityById(id);
        enrollmentRequestRepository.delete(request);
        return enrollmentRequestDtoMapper.mapToResponseDto(request);
    }

}
