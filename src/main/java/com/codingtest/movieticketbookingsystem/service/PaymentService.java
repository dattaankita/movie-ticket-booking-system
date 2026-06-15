package com.codingtest.movieticketbookingsystem.service;

import com.codingtest.movieticketbookingsystem.common.enums.PaymentStatus;
import com.codingtest.movieticketbookingsystem.config.BookingProperties;
import com.codingtest.movieticketbookingsystem.domain.booking.Booking;
import com.codingtest.movieticketbookingsystem.domain.payment.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingProperties bookingProperties;

    public Payment processPayment(Booking booking) {
        PaymentStatus status = bookingProperties.payment().simulateFailure()
                ? PaymentStatus.FAILED
                : PaymentStatus.SUCCESS;

        return Payment.builder()
                .booking(booking)
                .amount(booking.getTotalAmount())
                .status(status)
                .transactionRef(status == PaymentStatus.SUCCESS
                        ? "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase()
                        : null)
                .build();
    }
}
