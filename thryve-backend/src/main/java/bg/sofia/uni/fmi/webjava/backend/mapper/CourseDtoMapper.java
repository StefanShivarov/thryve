package bg.sofia.uni.fmi.webjava.backend.mapper;

import bg.sofia.uni.fmi.webjava.backend.model.dto.CreateCourseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.UpdateCourseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.response.CourseResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Course;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CourseDtoMapper {

    Course mapDtoToCourse(CreateCourseDto createCourseDto);

    CourseResponseDto mapCourseToResponseDto(Course course);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCourseFromDto(UpdateCourseDto createCourseDto, @MappingTarget Course course);

}
