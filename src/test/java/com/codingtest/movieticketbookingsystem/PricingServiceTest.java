package com.codingtest.movieticketbookingsystem;

import com.codingtest.movieticketbookingsystem.common.enums.PricingTierType;
import com.codingtest.movieticketbookingsystem.common.enums.SeatCategory;
import com.codingtest.movieticketbookingsystem.domain.pricing.PricingTier;
import com.codingtest.movieticketbookingsystem.domain.seat.Seat;
import com.codingtest.movieticketbookingsystem.domain.show.Show;
import com.codingtest.movieticketbookingsystem.repository.PricingTierRepository;
import com.codingtest.movieticketbookingsystem.service.PricingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private PricingTierRepository pricingTierRepository;

    @InjectMocks
    private PricingService pricingService;

    @Test
    void calculatesPremiumSeatWithTierMultiplier() {
        Show show = Show.builder()
                .basePrice(new BigDecimal("200.00"))
                .pricingTier(PricingTier.builder().multiplier(new BigDecimal("1.50")).build())
                .build();

        Seat premiumSeat = Seat.builder()
                .id(1L)
                .category(SeatCategory.PREMIUM)
                .build();

        PricingService.PricingResult result = pricingService.calculate(show, List.of(premiumSeat));

        assertThat(result.subtotal()).isEqualByComparingTo("450.00");
        assertThat(result.unitPrices().get(1L)).isEqualByComparingTo("450.00");
    }

    @Test
    void calculatesRegularSeatWithoutTier() {
        Show show = Show.builder()
                .basePrice(new BigDecimal("200.00"))
                .startTime(Instant.parse("2026-06-17T18:00:00Z"))
                .build();

        Seat regularSeat = Seat.builder()
                .id(2L)
                .category(SeatCategory.REGULAR)
                .build();

        PricingService.PricingResult result = pricingService.calculate(show, List.of(regularSeat));

        assertThat(result.subtotal()).isEqualByComparingTo("200.00");
    }

    @Test
    void appliesWeekendTierAutomaticallyWhenShowHasNoTier() {
        Instant saturday = Instant.parse("2026-06-20T18:00:00Z");
        Show show = Show.builder()
                .basePrice(new BigDecimal("200.00"))
                .startTime(saturday)
                .build();

        when(pricingTierRepository.findFirstByTierTypeAndActiveTrue(PricingTierType.WEEKEND))
                .thenReturn(Optional.of(PricingTier.builder()
                        .tierType(PricingTierType.WEEKEND)
                        .multiplier(new BigDecimal("1.25"))
                        .active(true)
                        .build()));

        Seat regularSeat = Seat.builder()
                .id(3L)
                .category(SeatCategory.REGULAR)
                .build();

        PricingService.PricingResult result = pricingService.calculate(show, List.of(regularSeat));

        assertThat(result.subtotal()).isEqualByComparingTo("250.00");
    }
}
