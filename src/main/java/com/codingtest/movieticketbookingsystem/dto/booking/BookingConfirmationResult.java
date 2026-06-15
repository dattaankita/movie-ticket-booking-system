package com.codingtest.movieticketbookingsystem.dto.booking;

import lombok.Builder;

@Builder
public record BookingConfirmationResult(
        BookingResponse response,
        boolean created
) {
}
