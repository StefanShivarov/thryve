package bg.sofia.uni.fmi.webjava.backend.model.dto.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseCreateDto {

    @NotBlank
    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters long!")
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String imageUrl;

}
