package bg.sofia.uni.fmi.webjava.backend.model.dto;

import bg.sofia.uni.fmi.webjava.backend.model.dto.course.CoursePreviewDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserResponseDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class NotificationResponseDto {

    private UUID id;
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
    private UserResponseDto sender;
    private CoursePreviewDto course;

}
