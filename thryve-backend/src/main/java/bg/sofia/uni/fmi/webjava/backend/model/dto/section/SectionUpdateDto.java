package bg.sofia.uni.fmi.webjava.backend.model.dto.section;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SectionUpdateDto {

    @Size(max = 100, message = "Section title must be at most 100 characters long!")
    private String title;

    private String textContent;

    @PositiveOrZero(message = "Order number must be a non-negative integer!")
    private Integer orderNumber;

}
