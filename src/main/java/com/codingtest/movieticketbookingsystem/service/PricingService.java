package com.codingtest.movieticketbookingsystem.service;

import com.codingtest.movieticketbookingsystem.common.enums.PricingTierType;
import com.codingtest.movieticketbookingsystem.common.enums.SeatCategory;
import com.codingtest.movieticketbookingsystem.domain.pricing.PricingTier;
import com.codingtest.movieticketbookingsystem.domain.seat.Seat;
import com.codingtest.movieticketbookingsystem.domain.show.Show;
import com.codingtest.movieticketbookingsystem.repository.PricingTierRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PricingService {

    private static final BigDecimal PREMIUM_SEAT_MULTIPLIER = new BigDecimal("1.50");
    private static final BigDecimal REGULAR_SEAT_MULTIPLIER = BigDecimal.ONE;

    private final PricingTierRepository pricingTierRepository;

    public PricingResult calculate(Show show, List<Seat> seats) {
        BigDecimal tierMultiplier = resolveTierMultiplier(show);

        Map<Long, BigDecimal> unitPrices = new HashMap<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (Seat seat : seats) {
            BigDecimal unitPrice = calculateUnitPrice(show.getBasePrice(), tierMultiplier, seat.getCategory());
            unitPrices.put(seat.getId(), unitPrice);
            subtotal = subtotal.add(unitPrice);
        }

        return PricingResult.builder()
                .unitPrices(unitPrices)
                .subtotal(subtotal.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    public BigDecimal calculateUnitPrice(
            BigDecimal basePrice,
            BigDecimal tierMultiplier,
            SeatCategory category) {
        BigDecimal seatMultiplier = category == SeatCategory.PREMIUM
                ? PREMIUM_SEAT_MULTIPLIER
                : REGULAR_SEAT_MULTIPLIER;

        return basePrice
                .multiply(tierMultiplier)
                .multiply(seatMultiplier)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal resolveTierMultiplier(Show show) {
        if (show.getPricingTier() != null) {
            return show.getPricingTier().getMultiplier();
        }
        if (isWeekend(show.getStartTime())) {
            return pricingTierRepository.findFirstByTierTypeAndActiveTrue(PricingTierType.WEEKEND)
                    .map(PricingTier::getMultiplier)
                    .orElse(BigDecimal.ONE);
        }
        return BigDecimal.ONE;
    }

    private boolean isWeekend(Instant startTime) {
        if (startTime == null) {
            return false;
        }
        DayOfWeek day = startTime.atZone(ZoneOffset.UTC).getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    @Builder
    public record PricingResult(
            Map<Long, BigDecimal> unitPrices,
            BigDecimal subtotal
    ) {
    }
}
