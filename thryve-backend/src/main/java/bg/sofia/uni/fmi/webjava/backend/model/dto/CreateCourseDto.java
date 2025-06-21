package bg.sofia.uni.fmi.webjava.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseDto {

    @NotBlank
    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters long!")
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    @Pattern(
        regexp = "^https?://.*\\.(?:png|jpe?g|gif|bmp|webp|svg|tiff?)(\\?.*)?$",
        message = "Invalid image URL!"
    )
    private String imageUrl;

}
