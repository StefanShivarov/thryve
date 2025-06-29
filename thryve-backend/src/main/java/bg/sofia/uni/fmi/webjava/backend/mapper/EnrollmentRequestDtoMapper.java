package bg.sofia.uni.fmi.webjava.backend.mapper;

import bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.request.EnrollmentRequestResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.EnrollmentRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserDtoMapper.class, CourseDtoMapper.class})
public interface EnrollmentRequestDtoMapper {

    EnrollmentRequestResponseDto mapToResponseDto(EnrollmentRequest enrollmentRequest);

}
