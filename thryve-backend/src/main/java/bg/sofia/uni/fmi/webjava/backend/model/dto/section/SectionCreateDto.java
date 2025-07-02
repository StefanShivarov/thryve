package bg.sofia.uni.fmi.webjava.backend.model.dto.section;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SectionCreateDto {

    @NotBlank(message = "Section title cannot be blank!")
    @Size(max = 100, message = "Section title must be at most 100 characters long!")
    private String title;

    private String textContent;

    @NotNull(message = "Order number cannot be null!")
    @PositiveOrZero(message = "Order number must be a non-negative integer!")
    private int orderNumber;

}
