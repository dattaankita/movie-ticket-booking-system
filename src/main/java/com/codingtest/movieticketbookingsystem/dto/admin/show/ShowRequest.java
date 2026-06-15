package com.codingtest.movieticketbookingsystem.dto.admin.show;

import com.codingtest.movieticketbookingsystem.common.enums.ShowStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowRequest {

    @NotNull(message = "Theater id is required")
    private Long theaterId;

    @NotBlank(message = "Movie title is required")
    @Size(max = 255, message = "Movie title must be at most 255 characters")
    private String movieTitle;

    @NotNull(message = "Start time is required")
    private Instant startTime;

    @NotNull(message = "End time is required")
    private Instant endTime;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.01", message = "Base price must be greater than zero")
    private BigDecimal basePrice;

    private Long pricingTierId;

    private ShowStatus status;
}
