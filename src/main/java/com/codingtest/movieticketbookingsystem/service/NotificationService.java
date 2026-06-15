package com.codingtest.movieticketbookingsystem.service;

import com.codingtest.movieticketbookingsystem.config.BookingProperties;
import com.codingtest.movieticketbookingsystem.domain.booking.Booking;
import com.codingtest.movieticketbookingsystem.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final BookingRepository bookingRepository;
    private final BookingProperties bookingProperties;

    @Async
    public void sendBookingConfirmation(Long bookingId) {
        withBooking(bookingId, booking -> log.info(
                "[NOTIFICATION:BOOKING_CONFIRMATION] to user {} — Booking confirmed for '{}' on {}. Booking ID: {}. Total paid: {} {}.",
                booking.getUser().getEmail(),
                booking.getShow().getMovieTitle(),
                booking.getShow().getStartTime(),
                booking.getId(),
                booking.getTotalAmount(),
                bookingProperties.currency()
        ));
    }

    @Async
    public void sendCancellationNotification(Long bookingId) {
        withBooking(bookingId, booking -> log.info(
                "[NOTIFICATION:CANCELLATION] to user {} — Booking {} for '{}' has been cancelled.",
                booking.getUser().getEmail(),
                booking.getId(),
                booking.getShow().getMovieTitle()
        ));
    }

    @Async
    public void sendRefundNotification(Long bookingId, BigDecimal refundAmount) {
        withBooking(bookingId, booking -> log.info(
                "[NOTIFICATION:REFUND] to user {} — Refund of {} {} processed for booking {} ({}).",
                booking.getUser().getEmail(),
                refundAmount,
                bookingProperties.currency(),
                booking.getId(),
                booking.getShow().getMovieTitle()
        ));
    }

    private void withBooking(Long bookingId, java.util.function.Consumer<Booking> action) {
        try {
            bookingRepository.findById(bookingId).ifPresentOrElse(
                    action,
                    () -> log.warn("Skipping notification — booking {} not found", bookingId)
            );
        } catch (Exception ex) {
            log.error("Failed to send notification for booking {}", bookingId, ex);
        }
    }
}
