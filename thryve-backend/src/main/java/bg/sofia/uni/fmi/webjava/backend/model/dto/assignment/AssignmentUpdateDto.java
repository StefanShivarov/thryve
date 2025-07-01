package bg.sofia.uni.fmi.webjava.backend.model.dto.assignment;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class AssignmentUpdateDto {

    static final int MIN_TITLE_LENGTH = 2;
    static final int MAX_TITLE_LENGTH = 100;
    static final int MIN_DESCRIPTION_LENGTH = 10;
    static final int MAX_DESCRIPTION_LENGTH = 1000;
    @Size(min = MIN_TITLE_LENGTH, max = MAX_TITLE_LENGTH, message = "Title must be between 2 and 100 characters long!")
    private String title;

    @Size(min = MIN_DESCRIPTION_LENGTH, max = MAX_DESCRIPTION_LENGTH, message = "Description must be between 10 and 1000 characters long!")
    private String description;

    private LocalDateTime deadline;

    private UUID courseId;

}
