package bg.sofia.uni.fmi.webjava.backend.controller;

import bg.sofia.uni.fmi.webjava.backend.model.dto.MessageResponse;
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
        return ResponseEntity.ok(notificationService.getMyNotifications(auth.getName(), pageable));
    }

    @PreAuthorize("hasAnyRole('STANDARD','CREATOR','ADMIN')")
    @GetMapping("/me/unread-count")
    public ResponseEntity<Long> getMyUnreadCount(Authentication auth) {
        return ResponseEntity.ok(notificationService.getMyUnreadCount(auth.getName()));
    }

    @PreAuthorize("hasAnyRole('STANDARD','CREATOR','ADMIN')")
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id, Authentication auth) {
        notificationService.markAsRead(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('STANDARD','CREATOR','ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOne(@PathVariable UUID id, Authentication auth) {
        notificationService.remove(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('STANDARD','CREATOR','ADMIN')")
    @DeleteMapping("/me")
    public ResponseEntity<Void> clearMine(Authentication auth) {
        notificationService.clearMine(auth.getName());
        return ResponseEntity.noContent().build();
    }

}
