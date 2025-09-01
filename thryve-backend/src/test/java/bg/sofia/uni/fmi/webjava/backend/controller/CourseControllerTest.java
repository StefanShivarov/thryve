package bg.sofia.uni.fmi.webjava.backend.controller;

import bg.sofia.uni.fmi.webjava.backend.model.dto.EntityModificationResponse;
import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CourseCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CourseResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CourseUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.service.CourseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import static bg.sofia.uni.fmi.webjava.backend.controller.CourseController.COURSE_DELETED_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.controller.CourseController.COURSE_UPDATED_MESSAGE;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createTestCourseResponseDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseControllerTest {

    @Mock
    private CourseService courseService;

    @InjectMocks
    private CourseController courseController;

    private static final UUID COURSE_ID = UUID.randomUUID();
    private static final String CREATOR_EMAIL = "creator@test.com";
    private static final CourseResponseDto TEST_COURSE_RESPONSE_DTO = createTestCourseResponseDto();

    @Test
    void testGetAllCourses() {
        Page<CourseResponseDto> page = new PageImpl<>(List.of(TEST_COURSE_RESPONSE_DTO));
        when(courseService.getAllCourses(any())).thenReturn(page);

        ResponseEntity<Page<CourseResponseDto>> response =
            courseController.getAllCourses(0, 10, "id", "ASC");

        assertEquals(page, response.getBody());
        verify(courseService).getAllCourses(any());
    }

    @Test
    void testGetCourseById() {
        when(courseService.getCourseById(eq(COURSE_ID))).thenReturn(TEST_COURSE_RESPONSE_DTO);

        ResponseEntity<CourseResponseDto> response = courseController.getCourseById(COURSE_ID);

        assertEquals(TEST_COURSE_RESPONSE_DTO, response.getBody());
        verify(courseService).getCourseById(eq(COURSE_ID));
    }

    @Test
    void testCreateCourse() {
        CourseCreateDto courseCreateDto = new CourseCreateDto();
        Authentication authentication = mock();
        when(authentication.getName()).thenReturn(CREATOR_EMAIL);
        when(courseService.createCourse(eq(courseCreateDto), eq(CREATOR_EMAIL))).thenReturn(TEST_COURSE_RESPONSE_DTO);

        ResponseEntity<CourseResponseDto> response = courseController.createCourse(courseCreateDto, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(TEST_COURSE_RESPONSE_DTO, response.getBody());
        verify(courseService).createCourse(eq(courseCreateDto), eq(CREATOR_EMAIL));
    }

    @Test
    void testUpdateCourseById() {
        CourseUpdateDto courseUpdateDto = new CourseUpdateDto();
        Authentication authentication = mock();
        when(authentication.getName()).thenReturn(CREATOR_EMAIL);
        when(courseService.updateCourseById(eq(COURSE_ID), eq(courseUpdateDto), eq(CREATOR_EMAIL)))
            .thenReturn(TEST_COURSE_RESPONSE_DTO);

        ResponseEntity<EntityModificationResponse<CourseResponseDto>> response =
            courseController.updateCourseById(COURSE_ID, courseUpdateDto, authentication);

        assertNotNull(response.getBody());
        assertEquals(COURSE_UPDATED_MESSAGE, response.getBody().getMessage());
        assertEquals(TEST_COURSE_RESPONSE_DTO, response.getBody().getData());
        verify(courseService).updateCourseById(eq(COURSE_ID), eq(courseUpdateDto), eq(CREATOR_EMAIL));
    }

    @Test
    void testDeleteCourseById() {
        when(courseService.deleteCourseById(eq(COURSE_ID))).thenReturn(TEST_COURSE_RESPONSE_DTO);

        ResponseEntity<EntityModificationResponse<CourseResponseDto>> response =
            courseController.deleteCourseById(COURSE_ID);

        assertNotNull(response.getBody());
        assertEquals(COURSE_DELETED_MESSAGE, response.getBody().getMessage());
        assertEquals(TEST_COURSE_RESPONSE_DTO, response.getBody().getData());
        verify(courseService).deleteCourseById(eq(COURSE_ID));
    }

}
