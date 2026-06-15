package com.codingtest.movieticketbookingsystem.dto.admin.show;

import com.codingtest.movieticketbookingsystem.common.enums.ShowStatus;
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
public class ShowResponse {

    private Long id;
    private Long theaterId;
    private String theaterName;
    private String movieTitle;
    private Instant startTime;
    private Instant endTime;
    private BigDecimal basePrice;
    private Long pricingTierId;
    private String pricingTierName;
    private ShowStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
