package com.codingtest.movieticketbookingsystem.service;

import com.codingtest.movieticketbookingsystem.common.exception.BusinessException;
import com.codingtest.movieticketbookingsystem.domain.refund.RefundPolicy;
import com.codingtest.movieticketbookingsystem.domain.show.Show;
import com.codingtest.movieticketbookingsystem.repository.RefundPolicyRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundPolicyEngine {

    private final RefundPolicyRepository refundPolicyRepository;

    @Transactional(readOnly = true)
    public RefundCalculation calculate(Show show, BigDecimal totalAmount) {
        Instant now = Instant.now();
        if (!show.getStartTime().isAfter(now)) {
            throw new BusinessException(
                    "Cannot refund a booking for a show that has already started",
                    HttpStatus.BAD_REQUEST,
                    "SHOW_ALREADY_STARTED"
            );
        }

        long minutesUntilShow = Duration.between(now, show.getStartTime()).toMinutes();
        List<RefundPolicy> policies = refundPolicyRepository.findByActiveTrueOrderByHoursBeforeShowDesc();

        RefundPolicy applicablePolicy = policies.stream()
                .filter(policy -> minutesUntilShow >= policy.getHoursBeforeShow() * 60L)
                .findFirst()
                .orElse(null);

        if (applicablePolicy == null) {
            return RefundCalculation.builder()
                    .refundPolicy(null)
                    .refundPercentage(BigDecimal.ZERO)
                    .refundAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .minutesUntilShow(minutesUntilShow)
                    .build();
        }

        BigDecimal refundAmount = totalAmount
                .multiply(applicablePolicy.getRefundPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return RefundCalculation.builder()
                .refundPolicy(applicablePolicy)
                .refundPercentage(applicablePolicy.getRefundPercentage())
                .refundAmount(refundAmount)
                .minutesUntilShow(minutesUntilShow)
                .build();
    }

    @Builder
    public record RefundCalculation(
            RefundPolicy refundPolicy,
            BigDecimal refundPercentage,
            BigDecimal refundAmount,
            long minutesUntilShow
    ) {
    }
}
