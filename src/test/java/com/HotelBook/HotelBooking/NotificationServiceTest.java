package com.HotelBook.HotelBooking;



import com.HotelBook.HotelBooking.Notification.*;
import com.HotelBook.HotelBooking.User.entity.User;
import com.HotelBook.HotelBooking.User.repository.UserRepository;
import com.HotelBook.HotelBooking.Common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private MockEmailSender emailSender;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("guest@example.com");
    }

    @Test
    @DisplayName("send: Should set status to SENT on successful email delivery")
    void send_Success() {
        // Arrange
        SendNotificationRequest request = new SendNotificationRequest();
        request.setRecipientId(userId);
        request.setType(NotificationType.BOOKING_CONFIRMED);
        request.setBookingId(UUID.randomUUID());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        // Capture the notification passed to save
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        NotificationResponse response = notificationService.send(request);

        // Assert
        assertEquals(NotificationStatus.SENT, response.getStatus());
        verify(emailSender).send(eq("guest@example.com"), anyString(), anyString());
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    @DisplayName("send: Should set status to FAILED and persist when email sender throws exception")
    void send_EmailFailure_PersistsAsFailed() {
        // Arrange
        SendNotificationRequest request = new SendNotificationRequest();
        request.setRecipientId(userId);
        request.setType(NotificationType.PAYMENT_FAILED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        // Force the email sender to fail
        doThrow(new RuntimeException("SMTP Timeout")).when(emailSender).send(any(), any(), any());

        // Act
        NotificationResponse response = notificationService.send(request);

        // Assert
        assertEquals(NotificationStatus.FAILED, response.getStatus());
        verify(notificationRepository, times(2)).save(any(Notification.class));

        // Verify the error message was captured
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, atLeastOnce()).save(captor.capture());
        assertTrue(captor.getValue().getErrorMessage().contains("SMTP Timeout"));
    }

    @Test
    @DisplayName("sendBookingConfirmation: Should not throw exception even if inner send fails")
    void sendBookingConfirmation_GracefulFailure() {
        // Arrange
        when(userRepository.findById(any())).thenThrow(new RuntimeException("DB Down"));

        // Act & Assert
        assertDoesNotThrow(() ->
                notificationService.sendBookingConfirmation(UUID.randomUUID(), userId)
        );
    }
}
