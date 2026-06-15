package com.codingtest.movieticketbookingsystem;

import com.codingtest.movieticketbookingsystem.common.enums.BookingStatus;
import com.codingtest.movieticketbookingsystem.common.exception.BusinessException;
import com.codingtest.movieticketbookingsystem.config.BookingProperties;
import com.codingtest.movieticketbookingsystem.domain.booking.Booking;
import com.codingtest.movieticketbookingsystem.repository.BookingRepository;
import com.codingtest.movieticketbookingsystem.repository.RefundRepository;
import com.codingtest.movieticketbookingsystem.service.CancellationService;
import com.codingtest.movieticketbookingsystem.service.NotificationService;
import com.codingtest.movieticketbookingsystem.service.RefundPolicyEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancellationServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private RefundPolicyEngine refundPolicyEngine;

    @Mock
    private NotificationService notificationService;

    @Mock
    private BookingProperties bookingProperties;

    @InjectMocks
    private CancellationService cancellationService;

    @Test
    void cancelBookingRejectsNonConfirmedStatus() {
        Booking booking = Booking.builder()
                .id(1L)
                .status(BookingStatus.PENDING)
                .build();

        when(bookingRepository.findByIdAndUserIdForUpdate(1L, 5L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> cancellationService.cancelBooking(1L, 5L, null))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> org.assertj.core.api.Assertions.assertThat(
                        ((BusinessException) ex).getErrorCode()).isEqualTo("BOOKING_NOT_CANCELLABLE"));

        verify(refundRepository, never()).save(any());
    }

    @Test
    void cancelBookingRejectsAlreadyCancelledBooking() {
        Booking booking = Booking.builder()
                .id(1L)
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingRepository.findByIdAndUserIdForUpdate(1L, 5L)).thenReturn(Optional.of(booking));
        when(refundRepository.existsByBooking_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> cancellationService.cancelBooking(1L, 5L, null))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> org.assertj.core.api.Assertions.assertThat(
                        ((BusinessException) ex).getErrorCode()).isEqualTo("BOOKING_ALREADY_CANCELLED"));
    }
}
