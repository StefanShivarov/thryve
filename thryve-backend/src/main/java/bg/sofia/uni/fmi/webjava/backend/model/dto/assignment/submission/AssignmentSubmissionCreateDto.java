package bg.sofia.uni.fmi.webjava.backend.model.dto.assignment.submission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSubmissionCreateDto {

    @NotBlank
    private String submissionUrl;

    @Size(min = 1, max = 1000)
    private String comment;

    @NotNull
    private UUID userId;

}
