package bg.sofia.uni.fmi.webjava.backend.mapper;

import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.EnrollmentCreateDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.EnrollmentResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.EnrollmentUpdateDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Enrollment;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {UserDtoMapper.class, CourseDtoMapper.class})
public interface EnrollmentDtoMapper {

    Enrollment mapDtoToEnrollment(EnrollmentCreateDto enrollmentCreateDto);

    EnrollmentResponseDto mapToResponseDto(Enrollment enrollment);

    void updateEnrollmentFromDto(EnrollmentUpdateDto enrollmentUpdateDto, @MappingTarget Enrollment enrollment);

}
