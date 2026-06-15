package com.codingtest.movieticketbookingsystem.service;

import com.codingtest.movieticketbookingsystem.common.enums.BookingStatus;
import com.codingtest.movieticketbookingsystem.common.enums.HoldStatus;
import com.codingtest.movieticketbookingsystem.common.enums.SeatAvailabilityStatus;
import com.codingtest.movieticketbookingsystem.repository.BookingSeatRepository;
import com.codingtest.movieticketbookingsystem.repository.SeatHoldItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SeatAvailabilityService {

    private final SeatHoldItemRepository seatHoldItemRepository;
    private final BookingSeatRepository bookingSeatRepository;

    @Transactional(readOnly = true)
    public SeatAvailabilityStatus getStatus(Long showId, Long seatId) {
        return getStatusesForShow(showId, List.of(seatId)).get(seatId);
    }

    @Transactional(readOnly = true)
    public Map<Long, SeatAvailabilityStatus> getStatusesForShow(Long showId, List<Long> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            return Map.of();
        }

        Instant now = Instant.now();
        Set<Long> bookedSeatIds = new HashSet<>(
                bookingSeatRepository.findBookedSeatIdsForShow(showId, seatIds, BookingStatus.CONFIRMED)
        );
        Set<Long> heldSeatIds = new HashSet<>(
                seatHoldItemRepository.findHeldSeatIdsForShowAndSeats(showId, seatIds, HoldStatus.ACTIVE, now)
        );

        Map<Long, SeatAvailabilityStatus> statuses = new HashMap<>();
        for (Long seatId : seatIds) {
            if (bookedSeatIds.contains(seatId)) {
                statuses.put(seatId, SeatAvailabilityStatus.BOOKED);
            } else if (heldSeatIds.contains(seatId)) {
                statuses.put(seatId, SeatAvailabilityStatus.HELD);
            } else {
                statuses.put(seatId, SeatAvailabilityStatus.AVAILABLE);
            }
        }
        return statuses;
    }

    @Transactional(readOnly = true)
    public void validateSeatsAvailable(Long showId, List<Long> seatIds) {
        validateSeatsAvailable(showId, seatIds, null);
    }

    @Transactional(readOnly = true)
    public void validateSeatsAvailable(Long showId, List<Long> seatIds, Long excludeHoldId) {
        if (seatIds == null || seatIds.isEmpty()) {
            return;
        }

        Instant now = Instant.now();
        Set<Long> bookedSeatIds = new HashSet<>(
                bookingSeatRepository.findBookedSeatIdsForShow(showId, seatIds, BookingStatus.CONFIRMED)
        );
        List<Long> heldSeatIdsList = excludeHoldId != null
                ? seatHoldItemRepository.findHeldSeatIdsForShowAndSeatsExcludingHold(
                        showId, seatIds, HoldStatus.ACTIVE, now, excludeHoldId)
                : seatHoldItemRepository.findHeldSeatIdsForShowAndSeats(
                        showId, seatIds, HoldStatus.ACTIVE, now);
        Set<Long> heldSeatIds = new HashSet<>(heldSeatIdsList);

        for (Long seatId : seatIds) {
            if (bookedSeatIds.contains(seatId) || heldSeatIds.contains(seatId)) {
                throwSeatUnavailable(seatId);
            }
        }
    }

    private void throwSeatUnavailable(Long seatId) {
        throw new com.codingtest.movieticketbookingsystem.common.exception.BusinessException(
                "Seat is not available: " + seatId,
                org.springframework.http.HttpStatus.CONFLICT,
                "SEAT_NOT_AVAILABLE"
        );
    }
}
