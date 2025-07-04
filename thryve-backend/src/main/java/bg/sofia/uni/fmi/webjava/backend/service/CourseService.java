package bg.sofia.uni.fmi.webjava.backend.service;

import bg.sofia.uni.fmi.webjava.backend.exception.EntityNotFoundException;
import bg.sofia.uni.fmi.webjava.backend.mapper.CourseDtoMapper;
import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CourseCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CourseUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CourseResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Course;
import bg.sofia.uni.fmi.webjava.backend.repository.CourseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class CourseService {

    public static final String COURSE_NOT_FOUND_ERROR_MESSAGE = "Course with id %s was not found!";

    private final CourseRepository courseRepository;
    private final CourseDtoMapper courseDtoMapper;

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
    public CourseResponseDto createCourse(CourseCreateDto courseCreateDto) {
        Course course = courseDtoMapper.mapDtoToCourse(courseCreateDto);
        return courseDtoMapper.mapCourseToResponseDto(courseRepository.save(course));
    }

    @Transactional
    public CourseResponseDto updateCourseById(UUID id, CourseUpdateDto courseUpdateDto) {
        Course course = getCourseEntityById(id);
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
