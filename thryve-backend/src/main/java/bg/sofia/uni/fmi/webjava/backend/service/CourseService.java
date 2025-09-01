package bg.sofia.uni.fmi.webjava.backend.service;

import bg.sofia.uni.fmi.webjava.backend.exception.EntityNotFoundException;
import bg.sofia.uni.fmi.webjava.backend.mapper.CourseDtoMapper;
import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CourseCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CourseUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CourseResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Course;
import bg.sofia.uni.fmi.webjava.backend.model.entity.User;
import bg.sofia.uni.fmi.webjava.backend.repository.CourseRepository;
import bg.sofia.uni.fmi.webjava.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static bg.sofia.uni.fmi.webjava.backend.service.UserService.USER_WITH_EMAIL_NOT_FOUND_ERROR_MESSAGE;
import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class CourseService {

    public static final String COURSE_NOT_FOUND_ERROR_MESSAGE = "Course with id %s was not found!";
    public static final String COURSE_UPDATED_MESSAGE = "Course details were updated";

    private final CourseRepository courseRepository;
    private final CourseDtoMapper courseDtoMapper;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public Page<CourseResponseDto> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable)
            .map(courseDtoMapper::mapCourseToResponseDto);
    }

    @Transactional
    public CourseResponseDto getCourseById(UUID id) {
        return courseDtoMapper.mapCourseToResponseDto(getCourseEntityById(id));
    }

    @Transactional
    public Course getCourseEntityById(UUID id) {
        return courseRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(format(COURSE_NOT_FOUND_ERROR_MESSAGE, id)));
    }

    @Transactional
    public CourseResponseDto createCourse(CourseCreateDto courseCreateDto, String creatorEmail) {
        User sender = userRepository.findByEmail(creatorEmail).orElseThrow(
            () -> new EntityNotFoundException(
                format(USER_WITH_EMAIL_NOT_FOUND_ERROR_MESSAGE, creatorEmail))
        );
        Course course = courseRepository.save(courseDtoMapper.mapDtoToCourse(courseCreateDto));
        notificationService.notifyAllUsersCourseCreated(course, sender);
        return courseDtoMapper.mapCourseToResponseDto(course);
    }

    @Transactional
    public CourseResponseDto updateCourseById(UUID id, CourseUpdateDto courseUpdateDto, String updaterEmail) {
        Course course = getCourseEntityById(id);
        userRepository.findByEmail(updaterEmail).ifPresent(sender ->
            notificationService.notifyEnrolledUsersCourseUpdated(course, sender, COURSE_UPDATED_MESSAGE));
        courseDtoMapper.updateCourseFromDto(courseUpdateDto, course);
        return courseDtoMapper.mapCourseToResponseDto(courseRepository.save(course));
    }

    @Transactional
    public CourseResponseDto deleteCourseById(UUID id) {
        CourseResponseDto courseResponseDto = getCourseById(id);
        courseRepository.deleteById(id);
        return courseResponseDto;
    }

}
