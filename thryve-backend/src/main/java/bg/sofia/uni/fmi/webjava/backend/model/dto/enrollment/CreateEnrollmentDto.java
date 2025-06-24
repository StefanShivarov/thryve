package bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment;

import bg.sofia.uni.fmi.webjava.backend.model.entity.EnrollmentType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEnrollmentDto {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID courseId;

    private EnrollmentType enrollmentType = EnrollmentType.STUDENT;

}
