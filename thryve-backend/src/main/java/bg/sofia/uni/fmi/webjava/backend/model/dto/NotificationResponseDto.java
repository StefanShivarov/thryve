package bg.sofia.uni.fmi.webjava.backend.model.dto;

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
    private String senderName;
    private boolean read;
    private LocalDateTime createdAt;
    private String senderEmail;
}
