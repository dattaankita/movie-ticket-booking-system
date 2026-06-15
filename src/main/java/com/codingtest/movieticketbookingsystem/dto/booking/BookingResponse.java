package com.codingtest.movieticketbookingsystem.dto.booking;

import com.codingtest.movieticketbookingsystem.common.enums.BookingStatus;
import com.codingtest.movieticketbookingsystem.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private Long showId;
    private String movieTitle;
    private Instant showStartTime;
    private BookingStatus status;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String currency;
    private String discountCode;
    private PaymentStatus paymentStatus;
    private String transactionRef;
    private List<BookingSeatResponse> seats;
    private Instant createdAt;
}
