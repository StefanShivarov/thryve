package bg.sofia.uni.fmi.webjava.backend.controller;

import bg.sofia.uni.fmi.webjava.backend.model.dto.MessageResponse;
import bg.sofia.uni.fmi.webjava.backend.model.dto.NotificationResponseDto;
import bg.sofia.uni.fmi.webjava.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PreAuthorize("hasAnyRole('STANDARD','CREATOR','ADMIN')")
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDto>> getMyNotifications(
        @RequestParam(defaultValue = "0") int pageNumber,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return ResponseEntity.ok(notificationService.getMyNotifications(pageable));
    }

    @PreAuthorize("hasAnyRole('STANDARD','CREATOR','ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteMyNotification(@PathVariable UUID id) {
        notificationService.deleteMyNotification(id);
        return ResponseEntity.ok(new MessageResponse("Notification deleted successfully!"));
    }

    @PreAuthorize("hasAnyRole('STANDARD','CREATOR','ADMIN')")
    @DeleteMapping
    public ResponseEntity<MessageResponse> deleteAllMyNotifications() {
        int removed = notificationService.deleteAllMyNotifications();
        return ResponseEntity.ok(new MessageResponse("Deleted " + removed + " notifications."));
    }
}
