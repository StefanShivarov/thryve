package bg.sofia.uni.fmi.webjava.backend.model.dto.course;

import bg.sofia.uni.fmi.webjava.backend.model.entity.Assignment;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Enrollment;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Notification;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CourseResponseDto {

    private UUID id;
    private String title;
    private String description;
    private String imageUrl;

}
