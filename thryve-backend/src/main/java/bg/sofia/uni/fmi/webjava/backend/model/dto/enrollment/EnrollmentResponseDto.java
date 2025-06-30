package bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment;

import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CoursePreviewDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.EnrollmentType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class EnrollmentResponseDto {

    private UUID id;
    private CoursePreviewDto course;
    private UserResponseDto user;
    private EnrollmentType enrollmentType;

}
