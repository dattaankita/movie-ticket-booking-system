package com.codingtest.movieticketbookingsystem.dto.admin.refund;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundPolicyRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;

    @NotNull(message = "Hours before show is required")
    @Min(value = 0, message = "Hours before show cannot be negative")
    private Integer hoursBeforeShow;

    @NotNull(message = "Refund percentage is required")
    @DecimalMin(value = "0.0", message = "Refund percentage must be at least 0")
    @DecimalMax(value = "100.0", message = "Refund percentage cannot exceed 100")
    private BigDecimal refundPercentage;

    private Boolean active;
}
