package com.codingtest.movieticketbookingsystem.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "booking")
public record BookingProperties(@Valid @NotNull Hold hold, @NotBlank String currency,
        @Valid @NotNull Payment payment) {
    public record Hold(@Positive int durationMinutes, @Positive long expiryCheckIntervalMs,
            @Positive int expiryBatchSize) {

    }

    public record Payment(boolean simulateFailure) {

    }
}
