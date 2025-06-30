package bg.sofia.uni.fmi.webjava.backend.model.dto.assignment;

import bg.sofia.uni.fmi.webjava.backend.config.JacksonObjectMapperConfiguration;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

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

    @NotNull(message = "Deadline cannot be null!")
    private LocalDateTime deadline;

}
