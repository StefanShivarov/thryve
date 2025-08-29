package bg.sofia.uni.fmi.webjava.backend.service;

import bg.sofia.uni.fmi.webjava.backend.model.dto.NotificationResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Notification;
import bg.sofia.uni.fmi.webjava.backend.repository.EnrollmentRepository;
import bg.sofia.uni.fmi.webjava.backend.repository.NotificationRepository;
import bg.sofia.uni.fmi.webjava.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import bg.sofia.uni.fmi.webjava.backend.model.entity.User;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService{

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    private UUID currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found")).getId();
    }

    @Transactional
    public Page<NotificationResponseDto> getMyNotifications(String email, Pageable pageable) {
        User me = userRepository.findByEmail(email).orElseThrow();
        return notificationRepository.findByRecipientId(me.getId(), pageable).map(this::toDto);
    }

    @Transactional
    public long getMyUnreadCount(String email) {
        User me = userRepository.findByEmail(email).orElseThrow();
        return notificationRepository.countByRecipientIdAndReadFalse(me.getId());
    }

    @Transactional
    public void markAsRead(UUID id, String email) {
        User me = userRepository.findByEmail(email).orElseThrow();
        Notification n = notificationRepository.findByIdAndRecipientId(id, me.getId())
            .orElseThrow(); // not found or not yours
        n.setRead(true);
        notificationRepository.save(n);
    }

    @Transactional
    public void remove(UUID id, String email) {
        User me = userRepository.findByEmail(email).orElseThrow();
        Notification n = notificationRepository.findByIdAndRecipientId(id, me.getId())
            .orElseThrow();
        notificationRepository.delete(n);
    }

    @Transactional
    public void clearMine(String email) {
        User me = userRepository.findByEmail(email).orElseThrow();
        notificationRepository.deleteByRecipientId(me.getId());
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

    public void notifyAllUsersCourseCreated(Course course, User sender) {
        List<User> users = userRepository.findAll();
        List<Notification> batch = new ArrayList<>(users.size());
        for (User u : users) {
            Notification n = new Notification();
            n.setTitle("New course");
            n.setMessage(course.getTitle());
            n.setSender(sender);
            n.setRecipient(u);
            n.setCourse(course);
            n.setRead(false);
            batch.add(n);
        }
        notificationRepository.saveAll(batch);
    }

    public void notifyEnrolledUsersCourseUpdated(Course course, User sender, String message) {
        var enrolled = enrollmentRepository.findByCourse(course);
        if (enrolled.isEmpty()) return;

        List<Notification> batch = new ArrayList<>();
        for (var e : enrolled) {
            var u = e.getUser();
            if (u == null) continue;

            Notification n = new Notification();
            n.setTitle("Course updated");
            n.setMessage(message != null ? message : course.getTitle());
            n.setSender(sender);
            n.setRecipient(u);
            n.setCourse(course);
            n.setRead(false);
            n.setCreatedAt(LocalDateTime.now()); // or rely on BaseEntity
            batch.add(n);
        }
        notificationRepository.saveAll(batch);
    }

    private NotificationResponseDto toDto(Notification n) {
        NotificationResponseDto dto = new NotificationResponseDto();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setRead(n.isRead());

        dto.setCreatedAt(n.getCreatedAt());

        if (n.getSender() != null) {
            String first = n.getSender().getFirstName() == null ? "" : n.getSender().getFirstName().trim();
            String last  = n.getSender().getLastName()  == null ? "" : n.getSender().getLastName().trim();
            String full  = (first + " " + last).trim();

            if (full.isEmpty()) {

                String fallbackName = (n.getSender().getUsername() != null && !n.getSender().getUsername().isBlank())
                    ? n.getSender().getUsername().trim()
                    : n.getSender().getEmail();
                dto.setSenderName(fallbackName);
            } else {
                dto.setSenderName(full);
            }

            dto.setSenderEmail(n.getSender().getEmail());
        }

        return dto;
    }

}
