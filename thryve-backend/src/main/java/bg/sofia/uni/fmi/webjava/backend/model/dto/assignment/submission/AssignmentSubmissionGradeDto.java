package bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.submission;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AssignmentSubmissionGradeDto {

    @Min(value = 0, message = "Grade must not be a negative number!")
    private double grade;

    @Size(min = 1, max = 1000, message = "Feedback must be between 1 and 1000 characters long!")
    private String feedback;

}
