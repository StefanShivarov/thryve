package bg.sofia.uni.fmi.webjava.backend.service;

import bg.sofia.uni.fmi.webjava.backend.exception.EntityAlreadyExistsException;
import bg.sofia.uni.fmi.webjava.backend.exception.EntityNotFoundException;
import bg.sofia.uni.fmi.webjava.backend.mapper.EnrollmentDtoMapper;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.CreateEnrollmentDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.EnrollmentResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.UpdateEnrollmentDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Course;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Enrollment;
import bg.sofia.uni.fmi.webjava.backend.model.entity.User;
import bg.sofia.uni.fmi.webjava.backend.repository.EnrollmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    public static final String ENROLLMENT_NOT_FOUND_ERROR_MESSAGE = "Enrollment with id %s was not found!";

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentDtoMapper enrollmentDtoMapper;
    private final CourseService courseService;
    private final UserService userService;

    @Transactional
    public Page<EnrollmentResponseDto> getAllEnrollments(Pageable pageable) {
        return enrollmentRepository.findAll(pageable)
            .map(enrollmentDtoMapper::mapToResponseDto);
    }

    @Transactional
    public Page<EnrollmentResponseDto> getEnrollmentsByCourseIdAndUserId(UUID courseId, UUID userId,
                                                                         Pageable pageable) {
        return enrollmentRepository.findEnrollmentByCourseIdAndUserId(courseId, userId, pageable)
            .map(enrollmentDtoMapper::mapToResponseDto);
    }

    @Transactional
    public Page<EnrollmentResponseDto> getEnrollmentsByCourseId(UUID courseId, Pageable pageable) {
        return enrollmentRepository.findEnrollmentsByCourseId(courseId, pageable)
            .map(enrollmentDtoMapper::mapToResponseDto);
    }

    @Transactional
    public Page<EnrollmentResponseDto> getEnrollmentsByUserId(UUID userId, Pageable pageable) {
        return enrollmentRepository.findEnrollmentsByUserId(userId, pageable)
            .map(enrollmentDtoMapper::mapToResponseDto);
    }

    @Transactional
    protected Enrollment getEnrollmentEntityById(UUID id) {
        return enrollmentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(format(ENROLLMENT_NOT_FOUND_ERROR_MESSAGE, id)));
    }

    @Transactional
    public EnrollmentResponseDto getEnrollmentById(UUID id) {
        return enrollmentDtoMapper.mapToResponseDto(getEnrollmentEntityById(id));
    }

    @Transactional
    public EnrollmentResponseDto createEnrollment(CreateEnrollmentDto dto) {
        Enrollment enrollment = enrollmentDtoMapper.mapDtoToEnrollment(dto);
        if (!getEnrollmentsByCourseIdAndUserId(dto.getCourseId(), dto.getUserId(), Pageable.unpaged()).isEmpty()) {
            throw new EntityAlreadyExistsException("Enrollment with userId %s and courseId %s already exists!");
        }
        Course course = courseService.getCourseEntityById(dto.getCourseId());
        User user = userService.getUserEntityById(dto.getUserId());
        enrollment.setCourse(course);
        enrollment.setUser(user);
        return enrollmentDtoMapper.mapToResponseDto(enrollmentRepository.save(enrollment));
    }

    @Transactional
    public EnrollmentResponseDto updateEnrollmentById(UUID id, UpdateEnrollmentDto updateEnrollmentDto) {
        Enrollment enrollment = getEnrollmentEntityById(id);
        enrollmentDtoMapper.updateEnrollmentFromDto(updateEnrollmentDto, enrollment);
        return enrollmentDtoMapper.mapToResponseDto(enrollmentRepository.save(enrollment));
    }

    @Transactional
    public EnrollmentResponseDto deleteEnrollmentById(UUID id) {
        EnrollmentResponseDto enrollmentResponseDto = getEnrollmentById(id);
        enrollmentRepository.deleteById(id);
        return enrollmentResponseDto;
    }

}
