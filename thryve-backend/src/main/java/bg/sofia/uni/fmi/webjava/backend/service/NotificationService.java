package bg.sofia.uni.fmi.webjava.backend.service;

import bg.sofia.uni.fmi.webjava.backend.model.dto.NotificationResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Notification;
import bg.sofia.uni.fmi.webjava.backend.repository.NotificationRepository;
import bg.sofia.uni.fmi.webjava.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService{

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private UUID currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found")).getId();
    }

    private NotificationResponseDto toDto(Notification n) {
        NotificationResponseDto dto = new NotificationResponseDto();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setRead(n.isRead());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> getMyNotifications(Pageable pageable) {
        UUID uid = currentUserId();
        return notificationRepository.findPageBySenderId(uid, pageable).map(this::toDto);
    }

    @Transactional
    public void deleteMyNotification(UUID notificationId) {
        UUID uid = currentUserId();
        Notification n = notificationRepository.findById(notificationId).orElseThrow(() -> new RuntimeException("Not found"));
        if (!n.getSender().getId().equals(uid)) throw new RuntimeException("Forbidden");
        notificationRepository.deleteById(notificationId);
    }

    @Transactional
    public int deleteAllMyNotifications() {
        UUID uid = currentUserId();
        return notificationRepository.deleteAllBySenderId(uid);
    }
}
