package bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.submission;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AssignmentSubmissionUpdateDto {

    private String submissionUrl;

    @Size(min = 1, max = 1000, message = "Comment must be between 1 and 1000 characters long!")
    private String comment;

}
