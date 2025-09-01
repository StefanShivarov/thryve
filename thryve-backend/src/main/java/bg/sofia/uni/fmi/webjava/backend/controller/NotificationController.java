package bg.sofia.uni.fmi.webjava.backend.controller;

import bg.sofia.uni.fmi.webjava.backend.model.dto.EntityModificationResponse;
import bg.sofia.uni.fmi.webjava.backend.model.dto.NotificationResponseDto;
import bg.sofia.uni.fmi.webjava.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    //TODO: remove /me endpoints

    private static final String NOTIFICATION_UPDATED_MESSAGE = "Notification marked as read.";
    private static final String NOTIFICATION_DELETED_MESSAGE = "Notification deleted successfully.";

    private final NotificationService notificationService;

    @PreAuthorize("hasAnyRole('STANDARD','CREATOR','ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<Page<NotificationResponseDto>> getMyNotifications(
        Authentication auth,
        @RequestParam(defaultValue = "0") int pageNumber,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(defaultValue = "postedAt") String sortBy,
        @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort.Direction dir = Sort.Direction.fromOptionalString(direction).orElse(Sort.Direction.DESC);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(dir, sortBy));
        return ResponseEntity.ok(notificationService.getNotificationsByRecipientEmail(auth.getName(), pageable));
    }

    @PreAuthorize("hasAnyRole('STANDARD','CREATOR','ADMIN')")
    @GetMapping("/me/unread-count")
    public ResponseEntity<Long> getMyUnreadCount(Authentication auth) {
        return ResponseEntity.ok(notificationService
            .getUnreadNotificationsCountByRecipientEmail(auth.getName())
        );
    }

    @PreAuthorize("hasAnyRole('STANDARD','CREATOR','ADMIN')")
    @PostMapping("/{id}/read")
    public ResponseEntity<EntityModificationResponse<NotificationResponseDto>> markAsRead(
        @PathVariable UUID id, Authentication auth
    ) {
        return ResponseEntity.ok(new EntityModificationResponse<>(
            NOTIFICATION_UPDATED_MESSAGE,
            notificationService.markNotificationAsReadByRecipientEmail(id, auth.getName())
        ));
    }

    @PreAuthorize("hasAnyRole('STANDARD','CREATOR','ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<EntityModificationResponse<NotificationResponseDto>> deleteNotificationById(
        @PathVariable UUID id, Authentication auth
    ) {
        return ResponseEntity.ok().body(new EntityModificationResponse<>(
            NOTIFICATION_DELETED_MESSAGE,
            notificationService.deleteNotificationById(id, auth.getName())
        ));
    }

    @PreAuthorize("hasAnyRole('STANDARD','CREATOR','ADMIN')")
    @DeleteMapping("/me")
    public ResponseEntity<Void> clearMine(Authentication auth) {
        notificationService.deleteNotificationsByRecipientEmail(auth.getName());
        return ResponseEntity.noContent().build();
    }

}
