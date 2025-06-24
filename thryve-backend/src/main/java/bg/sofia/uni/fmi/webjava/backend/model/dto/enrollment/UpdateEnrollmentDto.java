package bg.sofia.uni.fmi.webjava.backend.model.dto.enrollment;

import bg.sofia.uni.fmi.webjava.backend.model.entity.EnrollmentType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateEnrollmentDto {

    private EnrollmentType enrollmentType;

}
