package com.codingtest.movieticketbookingsystem;

import com.codingtest.movieticketbookingsystem.common.enums.DiscountType;
import com.codingtest.movieticketbookingsystem.common.exception.BusinessException;
import com.codingtest.movieticketbookingsystem.domain.discount.DiscountCode;
import com.codingtest.movieticketbookingsystem.repository.DiscountCodeRepository;
import com.codingtest.movieticketbookingsystem.service.DiscountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock
    private DiscountCodeRepository discountCodeRepository;

    @InjectMocks
    private DiscountService discountService;

    @Test
    void percentageDiscountIsCalculatedFromSubtotal() {
        DiscountCode code = DiscountCode.builder()
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10.00"))
                .build();

        BigDecimal discount = discountService.calculateDiscount(code, new BigDecimal("500.00"));

        assertThat(discount).isEqualByComparingTo("50.00");
    }

    @Test
    void fixedDiscountCannotExceedSubtotal() {
        DiscountCode code = DiscountCode.builder()
                .discountType(DiscountType.FIXED)
                .discountValue(new BigDecimal("100.00"))
                .build();

        BigDecimal discount = discountService.calculateDiscount(code, new BigDecimal("60.00"));

        assertThat(discount).isEqualByComparingTo("60.00");
    }

    @Test
    void lockActiveCodeRejectsExhaustedCodes() {
        DiscountCode code = DiscountCode.builder()
                .code("SAVE10")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10.00"))
                .validFrom(Instant.now().minus(1, ChronoUnit.DAYS))
                .validUntil(Instant.now().plus(1, ChronoUnit.DAYS))
                .maxUses(1)
                .usedCount(1)
                .active(true)
                .build();

        when(discountCodeRepository.findByCodeIgnoreCaseForUpdate("SAVE10")).thenReturn(Optional.of(code));

        assertThatThrownBy(() -> discountService.lockActiveCode("SAVE10"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo("DISCOUNT_CODE_EXHAUSTED"));
    }
}
