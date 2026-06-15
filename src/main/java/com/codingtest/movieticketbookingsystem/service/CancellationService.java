package com.codingtest.movieticketbookingsystem.service;

import com.codingtest.movieticketbookingsystem.common.enums.BookingStatus;
import com.codingtest.movieticketbookingsystem.common.enums.RefundStatus;
import com.codingtest.movieticketbookingsystem.common.exception.BusinessException;
import com.codingtest.movieticketbookingsystem.common.exception.ResourceNotFoundException;
import com.codingtest.movieticketbookingsystem.config.BookingProperties;
import com.codingtest.movieticketbookingsystem.domain.booking.Booking;
import com.codingtest.movieticketbookingsystem.domain.refund.Refund;
import com.codingtest.movieticketbookingsystem.dto.booking.CancelBookingRequest;
import com.codingtest.movieticketbookingsystem.dto.booking.CancelBookingResponse;
import com.codingtest.movieticketbookingsystem.repository.BookingRepository;
import com.codingtest.movieticketbookingsystem.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancellationService {

    private final BookingRepository bookingRepository;
    private final RefundRepository refundRepository;
    private final RefundPolicyEngine refundPolicyEngine;
    private final NotificationService notificationService;
    private final BookingProperties bookingProperties;

    @Transactional
    public CancelBookingResponse cancelBooking(Long bookingId, Long userId, CancelBookingRequest request) {
        Booking booking = bookingRepository.findByIdAndUserIdForUpdate(bookingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        validateCancellable(booking);

        RefundPolicyEngine.RefundCalculation calculation =
                refundPolicyEngine.calculate(booking.getShow(), booking.getTotalAmount());

        Refund refund = Refund.builder()
                .booking(booking)
                .refundPolicy(calculation.refundPolicy())
                .amount(calculation.refundAmount())
                .status(RefundStatus.PROCESSED)
                .reason(request != null ? request.getReason() : null)
                .build();

        refundRepository.save(refund);
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        notificationService.sendCancellationNotification(booking.getId());
        if (calculation.refundAmount().signum() > 0) {
            notificationService.sendRefundNotification(booking.getId(), calculation.refundAmount());
        }

        String policyName = calculation.refundPolicy() != null
                ? calculation.refundPolicy().getName()
                : "No refund";

        return CancelBookingResponse.builder()
                .bookingId(booking.getId())
                .bookingStatus(BookingStatus.CANCELLED)
                .refundId(refund.getId())
                .refundAmount(calculation.refundAmount())
                .refundPercentage(calculation.refundPercentage())
                .refundPolicyName(policyName)
                .refundStatus(RefundStatus.PROCESSED)
                .currency(bookingProperties.currency())
                .message(buildMessage(calculation))
                .build();
    }

    private void validateCancellable(Booking booking) {
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException(
                    "Only confirmed bookings can be cancelled",
                    HttpStatus.BAD_REQUEST,
                    "BOOKING_NOT_CANCELLABLE"
            );
        }

        if (refundRepository.existsByBooking_Id(booking.getId())) {
            throw new BusinessException(
                    "Booking has already been cancelled",
                    HttpStatus.CONFLICT,
                    "BOOKING_ALREADY_CANCELLED"
            );
        }
    }

    private String buildMessage(RefundPolicyEngine.RefundCalculation calculation) {
        if (calculation.refundAmount().signum() == 0) {
            return "Booking cancelled. No refund applies based on the current refund policy.";
        }
        return "Booking cancelled. Refund of " + calculation.refundAmount()
                + " " + bookingProperties.currency() + " has been processed.";
    }
}
