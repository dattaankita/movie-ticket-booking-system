package com.codingtest.movieticketbookingsystem.dto.admin.discount;

import com.codingtest.movieticketbookingsystem.common.enums.DiscountType;
import jakarta.validation.constraints.*;
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
public class DiscountCodeRequest {

    @NotBlank(message = "Code is required")
    @Size(max = 50, message = "Code must be at most 50 characters")
    private String code;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than zero")
    private BigDecimal discountValue;

    @NotNull(message = "Valid from is required")
    private Instant validFrom;

    @NotNull(message = "Valid until is required")
    private Instant validUntil;

    @Min(value = 1, message = "Max uses must be at least 1")
    private Integer maxUses;

    private Boolean active;
}
