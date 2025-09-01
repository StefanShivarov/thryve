package bg.sofia.uni.fmi.webjava.backend.controller;

import bg.sofia.uni.fmi.webjava.backend.model.dto.EntityModificationResponse;
import bg.sofia.uni.fmi.webjava.backend.model.dto.NotificationResponseDto;
import bg.sofia.uni.fmi.webjava.backend.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import static bg.sofia.uni.fmi.webjava.backend.utils.TestUtils.createTestNotificationResponseDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private static final NotificationResponseDto
        TEST_NOTIFICATION = createTestNotificationResponseDto();
    private static final UUID NOTIFICATION_ID = TEST_NOTIFICATION.getId();
    private static final String TEST_EMAIL = "test@example.com";

    @Test
    void testGetMyNotifications() {
        Authentication auth = mock();
        when(auth.getName()).thenReturn(TEST_EMAIL);
        Page<NotificationResponseDto> page = new PageImpl<>(List.of(TEST_NOTIFICATION));
        when(notificationService.getNotificationsByRecipientEmail(eq(TEST_EMAIL), any()))
            .thenReturn(page);

        ResponseEntity<Page<NotificationResponseDto>> response =
            notificationController.getMyNotifications(auth, 0, 10, "postedAt", "DESC");

        assertEquals(page, response.getBody());
        verify(notificationService).getNotificationsByRecipientEmail(eq(TEST_EMAIL), any());
    }

    @Test
    void testGetMyUnreadCount() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(TEST_EMAIL);
        when(notificationService.getUnreadNotificationsCountByRecipientEmail(TEST_EMAIL))
            .thenReturn(5L);

        ResponseEntity<Long> response = notificationController.getMyUnreadCount(auth);

        assertEquals(5L, response.getBody());
        verify(notificationService).getUnreadNotificationsCountByRecipientEmail(TEST_EMAIL);
    }

    @Test
    void testMarkAsRead() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(TEST_EMAIL);
        when(notificationService.markNotificationAsReadByRecipientEmail(NOTIFICATION_ID, TEST_EMAIL))
            .thenReturn(TEST_NOTIFICATION);

        ResponseEntity<EntityModificationResponse<NotificationResponseDto>> response =
            notificationController.markAsRead(NOTIFICATION_ID, auth);

        assertNotNull(response.getBody());
        assertEquals("Notification marked as read.", response.getBody().getMessage());
        assertEquals(TEST_NOTIFICATION, response.getBody().getData());
        verify(notificationService).markNotificationAsReadByRecipientEmail(NOTIFICATION_ID, TEST_EMAIL);
    }

    @Test
    void testDeleteNotificationById() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(TEST_EMAIL);
        when(notificationService.deleteNotificationById(NOTIFICATION_ID, TEST_EMAIL))
            .thenReturn(TEST_NOTIFICATION);

        ResponseEntity<EntityModificationResponse<NotificationResponseDto>> response =
            notificationController.deleteNotificationById(NOTIFICATION_ID, auth);

        assertNotNull(response.getBody());
        assertEquals("Notification deleted successfully.", response.getBody().getMessage());
        assertEquals(TEST_NOTIFICATION, response.getBody().getData());
        verify(notificationService).deleteNotificationById(NOTIFICATION_ID, TEST_EMAIL);
    }

    @Test
    void testClearMine() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(TEST_EMAIL);

        ResponseEntity<Void> response = notificationController.clearMine(auth);

        assertEquals(204, response.getStatusCode().value());
        verify(notificationService).deleteNotificationsByRecipientEmail(TEST_EMAIL);
    }

}
