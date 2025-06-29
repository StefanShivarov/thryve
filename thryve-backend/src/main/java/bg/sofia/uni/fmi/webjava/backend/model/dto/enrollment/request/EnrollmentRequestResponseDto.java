package bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment.request;

import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CoursePreviewDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.EnrollmentState;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class EnrollmentRequestResponseDto {

    private UUID id;
    private UserResponseDto user;
    private CoursePreviewDto course;
    private EnrollmentState state;
    
}
