package com.codingtest.movieticketbookingsystem.dto.browse;

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
public class BrowseShowResponse {

    private Long id;
    private Long theaterId;
    private String theaterName;
    private Long cityId;
    private String cityName;
    private String movieTitle;
    private Instant startTime;
    private Instant endTime;
    private BigDecimal basePrice;
    private String pricingTierName;
    private BigDecimal pricingMultiplier;
}
