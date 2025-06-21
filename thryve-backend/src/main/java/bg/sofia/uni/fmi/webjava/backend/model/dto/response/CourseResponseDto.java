package bg.sofia.uni.fmi.webjava.backend.model.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CourseResponseDto {

    private UUID id;
    private String title;
    private String description;
    private String imageUrl;
//    private Set<Enrollment> enrollments;
//    private Set<Assignment> assignments;
//    private Set<Notification> notifications;

}
