package bg.sofia.uni.fmi.webjava.backend.service;

import bg.sofia.uni.fmi.webjava.backend.exception.EntityNotFoundException;
import bg.sofia.uni.fmi.webjava.backend.mapper.NotificationDtoMapper;
import bg.sofia.uni.fmi.webjava.backend.model.dto.NotificationResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Enrollment;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Notification;
import bg.sofia.uni.fmi.webjava.backend.repository.EnrollmentRepository;
import bg.sofia.uni.fmi.webjava.backend.repository.NotificationRepository;
import bg.sofia.uni.fmi.webjava.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import bg.sofia.uni.fmi.webjava.backend.model.entity.User;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String NOTIFICATION_NOT_FOUND_MESSAGE = "Notification with id %s for user with id %s was not found!";

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final EnrollmentRepository enrollmentRepository;
    private final NotificationDtoMapper notificationDtoMapper;

    @Transactional
    public Page<NotificationResponseDto> getNotificationsByRecipientEmail(String email, Pageable pageable) {
        UserResponseDto user = userService.getUserByEmail(email);
        return notificationRepository.findByRecipientId(user.getId(), pageable).map(notificationDtoMapper::mapToResponseDto);
    }

    @Transactional
    public long getUnreadNotificationsCountByRecipientEmail(String email) {
        UserResponseDto user = userService.getUserByEmail(email);
        return notificationRepository.countByRecipientIdAndReadFalse(user.getId());
    }

    @Transactional
    public NotificationResponseDto markNotificationAsReadByRecipientEmail(UUID id, String email) {
        UserResponseDto user = userService.getUserByEmail(email);
        Notification notification = notificationRepository.findByIdAndRecipientId(id, user.getId())
            .orElseThrow(() -> new EntityNotFoundException(
                format(NOTIFICATION_NOT_FOUND_MESSAGE, id, user.getId())));
        notification.setRead(true);
        return notificationDtoMapper.mapToResponseDto(notificationRepository.save(notification));
    }

    @Transactional
    public NotificationResponseDto deleteNotificationById(UUID id, String email) {
        UserResponseDto user = userService.getUserByEmail(email);
        Notification notification = notificationRepository.findByIdAndRecipientId(id, user.getId())
            .orElseThrow(() -> new EntityNotFoundException(
                format(NOTIFICATION_NOT_FOUND_MESSAGE, id, user.getId())));
        notificationRepository.delete(notification);
        return notificationDtoMapper.mapToResponseDto(notification);
    }

    @Transactional
    public void deleteNotificationsByRecipientEmail(String email) {
        UserResponseDto user = userService.getUserByEmail(email);
        notificationRepository.deleteByRecipientId(user.getId());
    }

    public void notifyAllUsersCourseCreated(Course course, User sender) {
        //TODO: optimize or remove, keeping it just for the demo
        // This can lead to out of memory exception if there are too many users!
        List<User> users = userRepository.findAll();
        List<Notification> batch = new ArrayList<>(users.size());
        LocalDateTime notificationTime = LocalDateTime.now();
        for (User u : users) {
            Notification n = new Notification();
            n.setTitle("New course");
            n.setMessage(course.getTitle());
            n.setSender(sender);
            n.setRecipient(u);
            n.setCourse(course);
            n.setCreatedAt(notificationTime);
            n.setRead(false);
            batch.add(n);
        }
        notificationRepository.saveAll(batch);
    }

    public void notifyEnrolledUsersCourseUpdated(Course course, User sender, String message) {
        //TODO: this can also be optimized not to use repositories directly, and to throw adequate exceptions
        // keeping it for the demo to showcase notifications
        List<Enrollment> enrollments = enrollmentRepository.findByCourse(course);
        if (enrollments.isEmpty()) return;

        List<Notification> batch = new ArrayList<>();
        for (Enrollment e : enrollments) {
            User u = e.getUser();
            if (u == null) {
                continue;
            }
            Notification n = new Notification();
            n.setTitle("Course updated");
            n.setMessage(message != null ? message : course.getTitle());
            n.setSender(sender);
            n.setRecipient(u);
            n.setCourse(course);
            n.setRead(false);
            n.setCreatedAt(LocalDateTime.now());
            batch.add(n);
        }
        notificationRepository.saveAll(batch);
    }

}
