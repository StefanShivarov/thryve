package bg.sofia.uni.fmi.webjava.backend.mapper;

import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.CreateEnrollmentDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.EnrollmentResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.UpdateEnrollmentDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Enrollment;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {UserDtoMapper.class, CourseDtoMapper.class})
public interface EnrollmentDtoMapper {

    Enrollment mapDtoToEnrollment(CreateEnrollmentDto createEnrollmentDto);

    EnrollmentResponseDto mapToResponseDto(Enrollment enrollment);

    void updateEnrollmentFromDto(UpdateEnrollmentDto updateEnrollmentDto, @MappingTarget Enrollment enrollment);

}
