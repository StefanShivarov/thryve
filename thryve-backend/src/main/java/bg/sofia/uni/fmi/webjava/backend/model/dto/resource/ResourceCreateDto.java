package bg.sofia.uni.fmi.webjava.backend.model.dto.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceCreateDto {

    @NotBlank(message = "Resource name cannot be blank!")
    @Size(max = 200, message = "Resource name must be at most 200 characters long!")
    private String name;

    @NotBlank(message = "Resource URL cannot be blank!")
    @Pattern(
        regexp = "^https?://\\S+$",
        message = "Invalid resource URL!"
    )
    private String url;

}
