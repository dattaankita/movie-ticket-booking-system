package com.codingtest.movieticketbookingsystem.service;

import com.codingtest.movieticketbookingsystem.common.exception.BusinessException;
import com.codingtest.movieticketbookingsystem.domain.discount.DiscountCode;
import com.codingtest.movieticketbookingsystem.repository.DiscountCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountCodeRepository discountCodeRepository;

    @Transactional(readOnly = true)
    public DiscountCode resolveActiveCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        DiscountCode discountCode = discountCodeRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new BusinessException(
                        "Discount code not found",
                        HttpStatus.BAD_REQUEST,
                        "DISCOUNT_CODE_NOT_FOUND"
                ));

        validateDiscountCode(discountCode);
        return discountCode;
    }

    @Transactional
    public DiscountCode lockActiveCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        DiscountCode discountCode = discountCodeRepository.findByCodeIgnoreCaseForUpdate(code.trim())
                .orElseThrow(() -> new BusinessException(
                        "Discount code not found",
                        HttpStatus.BAD_REQUEST,
                        "DISCOUNT_CODE_NOT_FOUND"
                ));

        validateDiscountCode(discountCode);
        return discountCode;
    }

    public BigDecimal calculateDiscount(DiscountCode discountCode, BigDecimal subtotal) {
        if (discountCode == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = switch (discountCode.getDiscountType()) {
            case PERCENTAGE -> subtotal
                    .multiply(discountCode.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case FIXED -> discountCode.getDiscountValue();
        };

        return discount.min(subtotal).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private void validateDiscountCode(DiscountCode discountCode) {
        Instant now = Instant.now();

        if (!discountCode.isActive()) {
            throw new BusinessException(
                    "Discount code is inactive",
                    HttpStatus.BAD_REQUEST,
                    "DISCOUNT_CODE_INACTIVE"
            );
        }

        if (now.isBefore(discountCode.getValidFrom()) || !now.isBefore(discountCode.getValidUntil())) {
            throw new BusinessException(
                    "Discount code is not valid at this time",
                    HttpStatus.BAD_REQUEST,
                    "DISCOUNT_CODE_EXPIRED"
            );
        }

        if (discountCode.getMaxUses() != null
                && discountCode.getUsedCount() >= discountCode.getMaxUses()) {
            throw new BusinessException(
                    "Discount code usage limit reached",
                    HttpStatus.BAD_REQUEST,
                    "DISCOUNT_CODE_EXHAUSTED"
            );
        }
    }
}
