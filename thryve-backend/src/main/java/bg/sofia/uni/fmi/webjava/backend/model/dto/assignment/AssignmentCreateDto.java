package bg.sofia.uni.fmi.webjava.backend.model.dto.assignment;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentCreateDto {

    @NotBlank(message = "Title cannot be blank!")
    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters long!")
    private String title;

    @NotBlank(message = "Description cannot be blank!")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters long!")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(message = "Deadline cannot be null!")
    private LocalDateTime deadline;

    @NotNull(message = "Total points cannot be null!")
    @Positive(message = "Total points must be a positive number!")
    private double totalPoints;

}
