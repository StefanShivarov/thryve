package bg.sofia.uni.fmi.webjava.backend.model.dto.resource;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResourceUpdateDto {

    @Size(max = 200, message = "Resource name must be at most 200 characters long!")
    private String name;

    @Pattern(
        regexp = "^https?://\\S+$",
        message = "Invalid resource URL!"
    )
    private String url;

}
