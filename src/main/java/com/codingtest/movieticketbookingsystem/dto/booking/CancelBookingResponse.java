package com.codingtest.movieticketbookingsystem.dto.booking;

import com.codingtest.movieticketbookingsystem.common.enums.BookingStatus;
import com.codingtest.movieticketbookingsystem.common.enums.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelBookingResponse {

    private Long bookingId;
    private BookingStatus bookingStatus;
    private Long refundId;
    private BigDecimal refundAmount;
    private BigDecimal refundPercentage;
    private String refundPolicyName;
    private RefundStatus refundStatus;
    private String currency;
    private String message;
}
