package bg.sofia.uni.fmi.webjava.backend.model.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCourseDto {

    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters long!")
    private String title;

    private String description;

    @Pattern(
        regexp = "https?://.*\\.(?:png|jpe?g|gif|bmp|webp|svg|tiff?)(\\?.*)?$\n",
        message = "Invalid image URL!"
    )
    private String imageUrl;
}
