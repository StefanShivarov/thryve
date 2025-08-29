package bg.sofia.uni.fmi.webjava.backend.model.dto.course;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourseUpdateDto {

    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters long!")
    private String title;

    private String description;

    private String imageUrl;
}
