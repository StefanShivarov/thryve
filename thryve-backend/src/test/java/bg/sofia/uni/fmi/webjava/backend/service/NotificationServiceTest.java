package bg.sofia.uni.fmi.webjava.backend.service;

import bg.sofia.uni.fmi.webjava.backend.exception.EntityNotFoundException;
import bg.sofia.uni.fmi.webjava.backend.mapper.NotificationDtoMapper;
import bg.sofia.uni.fmi.webjava.backend.model.dto.NotificationResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.dto.user.UserResponseDto;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Course;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Enrollment;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Notification;
import bg.sofia.uni.fmi.webjava.backend.model.entity.User;
import bg.sofia.uni.fmi.webjava.backend.repository.EnrollmentRepository;
import bg.sofia.uni.fmi.webjava.backend.repository.NotificationRepository;
import bg.sofia.uni.fmi.webjava.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createStandardTestUser;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createStandardUserResponseDto;
import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createTestCourse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationDtoMapper notificationDtoMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private NotificationService notificationService;

    private static final User TEST_USER = createStandardTestUser();
    private static final UserResponseDto TEST_USER_RESPONSE_DTO = createStandardUserResponseDto();
    private static final Course TEST_COURSE = createTestCourse();
    private static final String EMAIL = TEST_USER.getEmail();
    private static final UUID USER_ID = TEST_USER.getId();
    private static final Notification TEST_NOTIFICATION = new Notification();
    private static final NotificationResponseDto TEST_NOTIFICATION_RESPONSE_DTO = new NotificationResponseDto();
    private static final UUID NOTIFICATION_ID = TEST_NOTIFICATION.getId();

    @Test
    void testGetNotificationsByRecipientEmail() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(TEST_NOTIFICATION));
        when(userService.getUserByEmail(eq(EMAIL))).thenReturn(TEST_USER_RESPONSE_DTO);
        when(notificationRepository.findByRecipientId(eq(USER_ID), eq(pageable))).thenReturn(page);
        when(notificationDtoMapper.mapToResponseDto(eq(TEST_NOTIFICATION))).thenReturn(TEST_NOTIFICATION_RESPONSE_DTO);

        Page<NotificationResponseDto> result = notificationService.getNotificationsByRecipientEmail(EMAIL, pageable);

        verify(userService).getUserByEmail(eq(EMAIL));
        verify(notificationRepository).findByRecipientId(eq(USER_ID), eq(pageable));
        verify(notificationDtoMapper).mapToResponseDto(eq(TEST_NOTIFICATION));
        assertNotNull(result);
        assertTrue(result.getContent().contains(TEST_NOTIFICATION_RESPONSE_DTO));
    }

    @Test
    void testGetUnreadNotificationsCountByRecipientEmail() {
        when(userService.getUserByEmail(eq(EMAIL))).thenReturn(TEST_USER_RESPONSE_DTO);
        when(notificationRepository.countByRecipientIdAndReadFalse(eq(USER_ID))).thenReturn(5L);

        long count = notificationService.getUnreadNotificationsCountByRecipientEmail(EMAIL);

        verify(userService).getUserByEmail(eq(EMAIL));
        verify(notificationRepository).countByRecipientIdAndReadFalse(eq(USER_ID));
        assertEquals(5L, count);
    }

    @Test
    void testMarkNotificationAsReadByRecipientEmail() {
        when(userService.getUserByEmail(eq(EMAIL))).thenReturn(TEST_USER_RESPONSE_DTO);
        when(notificationRepository.findByIdAndRecipientId(eq(NOTIFICATION_ID), eq(USER_ID)))
            .thenReturn(Optional.of(TEST_NOTIFICATION));
        when(notificationRepository.save(any(Notification.class))).thenReturn(TEST_NOTIFICATION);
        when(notificationDtoMapper.mapToResponseDto(eq(TEST_NOTIFICATION))).thenReturn(TEST_NOTIFICATION_RESPONSE_DTO);

        NotificationResponseDto result = notificationService.markNotificationAsReadByRecipientEmail(NOTIFICATION_ID, EMAIL);

        verify(userService).getUserByEmail(eq(EMAIL));
        verify(notificationRepository).findByIdAndRecipientId(eq(NOTIFICATION_ID), eq(USER_ID));
        verify(notificationRepository).save(any(Notification.class));
        verify(notificationDtoMapper).mapToResponseDto(eq(TEST_NOTIFICATION));
        assertEquals(TEST_NOTIFICATION_RESPONSE_DTO, result);
        assertTrue(TEST_NOTIFICATION.isRead());
    }

    @Test
    void testMarkNotificationAsReadByRecipientEmailNotFound() {
        when(userService.getUserByEmail(eq(EMAIL))).thenReturn(TEST_USER_RESPONSE_DTO);
        when(notificationRepository.findByIdAndRecipientId(eq(NOTIFICATION_ID), eq(USER_ID)))
            .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> notificationService.markNotificationAsReadByRecipientEmail(NOTIFICATION_ID, EMAIL));
    }

    @Test
    void testDeleteNotificationById() {
        when(userService.getUserByEmail(eq(EMAIL))).thenReturn(TEST_USER_RESPONSE_DTO);
        when(notificationRepository.findByIdAndRecipientId(eq(NOTIFICATION_ID), eq(USER_ID)))
            .thenReturn(Optional.of(TEST_NOTIFICATION));
        doNothing().when(notificationRepository).delete(eq(TEST_NOTIFICATION));
        when(notificationDtoMapper.mapToResponseDto(eq(TEST_NOTIFICATION))).thenReturn(TEST_NOTIFICATION_RESPONSE_DTO);

        NotificationResponseDto result = notificationService.deleteNotificationById(NOTIFICATION_ID, EMAIL);

        verify(userService).getUserByEmail(eq(EMAIL));
        verify(notificationRepository).findByIdAndRecipientId(eq(NOTIFICATION_ID), eq(USER_ID));
        verify(notificationRepository).delete(eq(TEST_NOTIFICATION));
        verify(notificationDtoMapper).mapToResponseDto(eq(TEST_NOTIFICATION));
        assertEquals(TEST_NOTIFICATION_RESPONSE_DTO, result);
    }

    @Test
    void testDeleteNotificationByIdNotFound() {
        when(userService.getUserByEmail(eq(EMAIL))).thenReturn(TEST_USER_RESPONSE_DTO);
        when(notificationRepository.findByIdAndRecipientId(eq(NOTIFICATION_ID), eq(USER_ID)))
            .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> notificationService.deleteNotificationById(NOTIFICATION_ID, EMAIL));
    }

    @Test
    void testDeleteNotificationsByRecipientEmail() {
        when(userService.getUserByEmail(eq(EMAIL))).thenReturn(TEST_USER_RESPONSE_DTO);
        doNothing().when(notificationRepository).deleteByRecipientId(eq(USER_ID));

        notificationService.deleteNotificationsByRecipientEmail(EMAIL);

        verify(userService).getUserByEmail(eq(EMAIL));
        verify(notificationRepository).deleteByRecipientId(eq(USER_ID));
    }

    @Test
    void testNotifyAllUsersCourseCreated() {
        List<User> users = List.of(TEST_USER);
        when(userRepository.findAll()).thenReturn(users);
        when(notificationRepository.saveAll(anyList())).thenReturn(List.of(TEST_NOTIFICATION));

        notificationService.notifyAllUsersCourseCreated(TEST_COURSE, TEST_USER);

        verify(userRepository).findAll();
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    void testNotifyEnrolledUsersCourseUpdated() {
        Enrollment enrollment = new Enrollment();
        enrollment.setUser(TEST_USER);
        List<Enrollment> enrollments = List.of(enrollment);
        when(enrollmentRepository.findByCourse(eq(TEST_COURSE))).thenReturn(enrollments);
        when(notificationRepository.saveAll(anyList())).thenReturn(List.of(TEST_NOTIFICATION));

        notificationService.notifyEnrolledUsersCourseUpdated(TEST_COURSE, TEST_USER, "Course updated");

        verify(enrollmentRepository).findByCourse(eq(TEST_COURSE));
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    void testNotifyEnrolledUsersCourseUpdatedNoEnrollments() {
        when(enrollmentRepository.findByCourse(eq(TEST_COURSE))).thenReturn(Collections.emptyList());

        notificationService.notifyEnrolledUsersCourseUpdated(TEST_COURSE, TEST_USER, "Course updated");

        verify(enrollmentRepository).findByCourse(eq(TEST_COURSE));
        verifyNoMoreInteractions(notificationRepository);
    }

}
