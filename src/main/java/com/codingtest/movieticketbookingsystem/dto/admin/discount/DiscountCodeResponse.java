package com.codingtest.movieticketbookingsystem.dto.admin.discount;

import com.codingtest.movieticketbookingsystem.common.enums.DiscountType;
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
public class DiscountCodeResponse {

    private Long id;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private Instant validFrom;
    private Instant validUntil;
    private Integer maxUses;
    private Integer usedCount;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
