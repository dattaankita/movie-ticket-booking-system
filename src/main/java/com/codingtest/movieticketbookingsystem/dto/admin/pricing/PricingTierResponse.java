package com.codingtest.movieticketbookingsystem.dto.admin.pricing;

import com.codingtest.movieticketbookingsystem.common.enums.PricingTierType;
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
public class PricingTierResponse {

    private Long id;
    private String name;
    private PricingTierType tierType;
    private BigDecimal multiplier;
    private String description;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
