package com.codingtest.movieticketbookingsystem.dto.admin.pricing;

import com.codingtest.movieticketbookingsystem.common.enums.PricingTierType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingTierRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @NotNull(message = "Tier type is required")
    private PricingTierType tierType;

    @NotNull(message = "Multiplier is required")
    @DecimalMin(value = "0.01", message = "Multiplier must be greater than zero")
    private BigDecimal multiplier;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    private Boolean active;
}
