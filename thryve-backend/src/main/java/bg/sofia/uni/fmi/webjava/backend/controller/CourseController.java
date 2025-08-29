package bg.sofia.uni.fmi.webjava.backend.controller;

import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CourseCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CourseUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CourseResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.EntityModificationResponse;
import bg.sofia.uni.fmi.webjava.backend.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private static final String COURSE_UPDATED_MESSAGE = "Course updated successfully!";
    private static final String COURSE_DELETED_MESSAGE = "Course deleted successfully!";

    private final CourseService courseService;

    //@PreAuthorize("hasAnyRole('STANDARD', 'CREATOR', 'ADMIN')")
    @GetMapping(value = {"", "/"})
    public ResponseEntity<Page<CourseResponseDto>> getAllCourses(
        @RequestParam(defaultValue = "0") int pageNumber,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String direction
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction).orElse(Sort.Direction.ASC);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(courseService.getAllCourses(pageable));
    }

    //@PreAuthorize("hasAnyRole('STANDARD', 'CREATOR', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDto> getCourseById(@PathVariable UUID id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    //@PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @PostMapping(value = {"", "/"})
    public ResponseEntity<CourseResponseDto> createCourse(@RequestBody @Valid CourseCreateDto courseCreateDto) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(courseService.createCourse(courseCreateDto));
    }

    //@PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<EntityModificationResponse<CourseResponseDto>> updateCourseById(
        @PathVariable UUID id,
        @RequestBody CourseUpdateDto courseUpdateDto
    ) {
        return ResponseEntity.ok(
            new EntityModificationResponse<>(
                COURSE_UPDATED_MESSAGE,
                courseService.updateCourseById(id, courseUpdateDto))
        );
    }

    //@PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<EntityModificationResponse<CourseResponseDto>> deleteCourseById(@PathVariable UUID id) {
        return ResponseEntity.ok(
            new EntityModificationResponse<>(COURSE_DELETED_MESSAGE, courseService.deleteCourseById(id))
        );
    }

}
