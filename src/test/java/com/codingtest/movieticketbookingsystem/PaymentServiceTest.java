package com.codingtest.movieticketbookingsystem;

import com.codingtest.movieticketbookingsystem.common.enums.PaymentStatus;
import com.codingtest.movieticketbookingsystem.config.BookingProperties;
import com.codingtest.movieticketbookingsystem.domain.booking.Booking;
import com.codingtest.movieticketbookingsystem.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private BookingProperties bookingProperties;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void processPaymentSucceedsByDefault() {
        when(bookingProperties.payment()).thenReturn(new BookingProperties.Payment(false));

        Booking booking = Booking.builder()
                .totalAmount(new BigDecimal("250.00"))
                .build();

        var payment = paymentService.processPayment(booking);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(payment.getAmount()).isEqualByComparingTo("250.00");
        assertThat(payment.getTransactionRef()).startsWith("TXN-");
    }

    @Test
    void processPaymentFailsWhenSimulated() {
        when(bookingProperties.payment()).thenReturn(new BookingProperties.Payment(true));

        Booking booking = Booking.builder()
                .totalAmount(new BigDecimal("100.00"))
                .build();

        var payment = paymentService.processPayment(booking);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getTransactionRef()).isNull();
    }
}
