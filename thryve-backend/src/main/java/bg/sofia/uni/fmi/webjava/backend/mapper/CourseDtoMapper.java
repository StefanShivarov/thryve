package bg.sofia.uni.fmi.webjava.backend.mapper;

import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CourseCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CourseUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CoursePreviewDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CourseResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Course;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CourseDtoMapper {

    Course mapDtoToCourse(CourseCreateDto courseCreateDto);

    CourseResponseDto mapCourseToResponseDto(Course course);

    CoursePreviewDto mapCourseToPreviewDto(Course course);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCourseFromDto(CourseUpdateDto createCourseDto, @MappingTarget Course course);

}
