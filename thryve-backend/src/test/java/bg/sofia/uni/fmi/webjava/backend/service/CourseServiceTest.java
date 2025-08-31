package bg.sofia.uni.fmi.webjava.backend.service;

import bg.sofia.uni.fmi.webjava.backend.exception.EntityNotFoundException;
import bg.sofia.uni.fmi.webjava.backend.mapper.CourseDtoMapper;
import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CourseCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CourseResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CourseUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Course;
import bg.sofia.uni.fmi.webjava.backend.model.entity.User;
import bg.sofia.uni.fmi.webjava.backend.repository.CourseRepository;
import bg.sofia.uni.fmi.webjava.backend.repository.UserRepository;
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

import static bg.sofia.uni.fmi.webjava.backend.service.CourseService.COURSE_NOT_FOUND_ERROR_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.service.CourseService.COURSE_UPDATED_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.service.UserService.USER_WITH_EMAIL_NOT_FOUND_ERROR_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createStandardTestUser;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createTestCourse;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createTestCourseResponseDto;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CourseDtoMapper courseDtoMapper;

    @InjectMocks
    private CourseService courseService;

    private static final Course TEST_COURSE = createTestCourse();
    private static final CourseResponseDto TEST_COURSE_RESPONSE_DTO = createTestCourseResponseDto();
    private static final User TEST_USER = createStandardTestUser();

    @Test
    void testGetAllCourses() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Course> expectedCourses = List.of(TEST_COURSE);
        Page<Course> page = new PageImpl<>(expectedCourses);

        when(courseRepository.findAll(eq(pageable))).thenReturn(page);
        when(courseDtoMapper.mapCourseToResponseDto(eq(TEST_COURSE))).thenReturn(TEST_COURSE_RESPONSE_DTO);

        Page<CourseResponseDto> result = courseService.getAllCourses(pageable);

        verify(courseRepository, times(1)).findAll(eq(pageable));
        assertNotNull(result);
        assertEquals(expectedCourses.size(), result.getContent().size());
        assertTrue(result.getContent().contains(TEST_COURSE_RESPONSE_DTO));
    }

    @Test
    void testGetCourseEntityById() {
        when(courseRepository.findById(eq(TEST_COURSE.getId()))).thenReturn(Optional.of(TEST_COURSE));
        Course result = courseService.getCourseEntityById(TEST_COURSE.getId());
        verify(courseRepository, times(1)).findById(eq(TEST_COURSE.getId()));
        assertEquals(TEST_COURSE, result);
    }

    @Test
    void testGetCourseEntityByIdForNonExistingCourseId() {
        when(courseRepository.findById(eq(TEST_COURSE.getId()))).thenReturn(Optional.empty());
        Exception exception = assertThrows(EntityNotFoundException.class,
            () -> courseService.getCourseEntityById(TEST_COURSE.getId()));
        assertEquals(format(COURSE_NOT_FOUND_ERROR_MESSAGE, TEST_COURSE.getId()), exception.getMessage());
    }

    @Test
    void testGetCourseById() {
        when(courseRepository.findById(eq(TEST_COURSE.getId()))).thenReturn(Optional.of(TEST_COURSE));
        when(courseDtoMapper.mapCourseToResponseDto(eq(TEST_COURSE))).thenReturn(TEST_COURSE_RESPONSE_DTO);

        CourseResponseDto result = courseService.getCourseById(TEST_COURSE.getId());

        verify(courseRepository, times(1)).findById(eq(TEST_COURSE.getId()));
        verify(courseDtoMapper, times(1)).mapCourseToResponseDto(eq(TEST_COURSE));
        assertEquals(TEST_COURSE_RESPONSE_DTO, result);
    }

    @Test
    void testCreateCourse() {
        User creator = createStandardTestUser();
        when(userRepository.findByEmail(eq(creator.getEmail()))).thenReturn(Optional.of(creator));
        when(courseDtoMapper.mapDtoToCourse(any(CourseCreateDto.class))).thenReturn(TEST_COURSE);
        when(courseRepository.save(eq(TEST_COURSE))).thenReturn(TEST_COURSE);
        doNothing().when(notificationService).notifyAllUsersCourseCreated(any(), any());
        when(courseDtoMapper.mapCourseToResponseDto(eq(TEST_COURSE))).thenReturn(TEST_COURSE_RESPONSE_DTO);

        CourseResponseDto result = courseService.createCourse(new CourseCreateDto(), creator.getEmail());

        verify(userRepository, times(1)).findByEmail(eq(creator.getEmail()));
        verify(courseDtoMapper, times(1)).mapDtoToCourse(any(CourseCreateDto.class));
        verify(courseRepository, times(1)).save(eq(TEST_COURSE));
        verify(notificationService, times(1)).notifyAllUsersCourseCreated(eq(TEST_COURSE), eq(creator));
        verify(courseDtoMapper, times(1)).mapCourseToResponseDto(eq(TEST_COURSE));
        assertEquals(TEST_COURSE_RESPONSE_DTO, result);
    }

    @Test
    void testCreateCourseForNonExistingUserByEmail() {
        when(userRepository.findByEmail(eq(TEST_USER.getEmail()))).thenReturn(Optional.empty());
        Exception exception = assertThrows(EntityNotFoundException.class,
            () -> courseService.createCourse(new CourseCreateDto(), TEST_USER.getEmail()));
        assertEquals(format(USER_WITH_EMAIL_NOT_FOUND_ERROR_MESSAGE, TEST_USER.getEmail()), exception.getMessage());
    }

    @Test
    void testUpdateCourseById() {
        when(courseRepository.findById(eq(TEST_COURSE.getId()))).thenReturn(Optional.of(TEST_COURSE));
        when(userRepository.findByEmail(eq(TEST_USER.getEmail())))
            .thenReturn(Optional.of(createStandardTestUser()));
        doNothing().when(notificationService)
                   .notifyEnrolledUsersCourseUpdated(any(), any(), anyString());
        doNothing().when(courseDtoMapper)
                   .updateCourseFromDto(any(CourseUpdateDto.class), any(Course.class));
        when(courseRepository.save(eq(TEST_COURSE))).thenReturn(TEST_COURSE);
        when(courseDtoMapper.mapCourseToResponseDto(eq(TEST_COURSE))).thenReturn(TEST_COURSE_RESPONSE_DTO);

        CourseResponseDto result = courseService.updateCourseById(TEST_COURSE.getId(),
            new CourseUpdateDto(), TEST_USER.getEmail());

        verify(courseRepository, times(1)).findById(eq(TEST_COURSE.getId()));
        verify(userRepository, times(1)).findByEmail(eq(TEST_USER.getEmail()));
        verify(notificationService, times(1))
                   .notifyEnrolledUsersCourseUpdated(eq(TEST_COURSE), eq(TEST_USER), eq(COURSE_UPDATED_MESSAGE));
        verify(courseDtoMapper, times(1))
                   .updateCourseFromDto(any(CourseUpdateDto.class), eq(TEST_COURSE));
        verify(courseRepository, times(1)).save(eq(TEST_COURSE));
        verify(courseDtoMapper, times(1)).mapCourseToResponseDto(eq(TEST_COURSE));
        assertEquals(TEST_COURSE_RESPONSE_DTO, result);
    }

    @Test
    void testDeleteCourseById() {
        when(courseRepository.findById(eq(TEST_COURSE.getId())))
            .thenReturn(Optional.of(TEST_COURSE));
        when(courseDtoMapper.mapCourseToResponseDto(eq(TEST_COURSE)))
            .thenReturn(TEST_COURSE_RESPONSE_DTO);
        doNothing().when(courseRepository).deleteById(eq(TEST_COURSE.getId()));

        CourseResponseDto result = courseService.deleteCourseById(TEST_COURSE.getId());

        verify(courseRepository, times(1)).findById(eq(TEST_COURSE.getId()));
        verify(courseDtoMapper, times(1)).mapCourseToResponseDto(eq(TEST_COURSE));
        verify(courseRepository, times(1)).deleteById(eq(TEST_COURSE.getId()));
        assertEquals(TEST_COURSE_RESPONSE_DTO, result);
    }

}
