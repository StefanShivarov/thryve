package bg.sofia.uni.fmi.webjava.backend.model.dto.assignment;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class AssignmentUpdateDto {

    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters long!")
    private String title;

    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters long!")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deadline;

    @Positive(message = "Total points must be a positive number!")
    private double totalPoints;

    private UUID courseId;

}
