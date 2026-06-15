package com.codingtest.movieticketbookingsystem.service;

import com.codingtest.movieticketbookingsystem.common.enums.HoldStatus;
import com.codingtest.movieticketbookingsystem.config.BookingProperties;
import com.codingtest.movieticketbookingsystem.domain.hold.SeatHold;
import com.codingtest.movieticketbookingsystem.repository.SeatHoldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class HoldExpiryService {

    private final SeatHoldRepository seatHoldRepository;
    private final BookingProperties bookingProperties;

    @Transactional
    public int expireHolds() {
        int batchSize = bookingProperties.hold().expiryBatchSize();
        int totalExpired = 0;
        Page<SeatHold> page;

        do {
            page = seatHoldRepository.findExpiredActiveHoldsForUpdate(
                    HoldStatus.ACTIVE,
                    Instant.now(),
                    PageRequest.of(0, batchSize)
            );

            for (SeatHold hold : page.getContent()) {
                hold.setStatus(HoldStatus.EXPIRED);
            }

            totalExpired += page.getNumberOfElements();
        } while (page.hasNext());

        return totalExpired;
    }
}
