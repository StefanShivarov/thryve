package bg.sofia.uni.fmi.webjava.backend.model.dto.assignment;

import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CoursePreviewDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class AssignmentResponseDto {

    private UUID id;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private double totalPoints;
    private CoursePreviewDto course;

}
