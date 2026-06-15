package com.codingtest.movieticketbookingsystem.dto.admin.refund;

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
public class RefundPolicyResponse {

    private Long id;
    private String name;
    private Integer hoursBeforeShow;
    private BigDecimal refundPercentage;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
