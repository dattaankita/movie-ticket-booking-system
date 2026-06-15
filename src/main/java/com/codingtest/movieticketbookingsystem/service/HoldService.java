package com.codingtest.movieticketbookingsystem.service;

import com.codingtest.movieticketbookingsystem.common.enums.HoldStatus;
import com.codingtest.movieticketbookingsystem.common.enums.ShowStatus;
import com.codingtest.movieticketbookingsystem.common.exception.BusinessException;
import com.codingtest.movieticketbookingsystem.common.exception.ResourceNotFoundException;
import com.codingtest.movieticketbookingsystem.config.BookingProperties;
import com.codingtest.movieticketbookingsystem.domain.hold.SeatHold;
import com.codingtest.movieticketbookingsystem.domain.hold.SeatHoldItem;
import com.codingtest.movieticketbookingsystem.domain.seat.Seat;
import com.codingtest.movieticketbookingsystem.domain.show.Show;
import com.codingtest.movieticketbookingsystem.domain.show.ShowSeat;
import com.codingtest.movieticketbookingsystem.domain.user.User;
import com.codingtest.movieticketbookingsystem.dto.hold.CreateHoldRequest;
import com.codingtest.movieticketbookingsystem.dto.hold.HoldResponse;
import com.codingtest.movieticketbookingsystem.dto.hold.HoldSeatResponse;
import com.codingtest.movieticketbookingsystem.repository.SeatHoldRepository;
import com.codingtest.movieticketbookingsystem.repository.ShowSeatRepository;
import com.codingtest.movieticketbookingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HoldService {

    private final SeatHoldRepository seatHoldRepository;
    private final ShowSeatRepository showSeatRepository;
    private final UserRepository userRepository;
    private final BrowseService browseService;
    private final SeatAvailabilityService seatAvailabilityService;
    private final BookingProperties bookingProperties;

    @Transactional
    public HoldResponse createHold(Long showId, CreateHoldRequest request, Long userId) {
        Show show = browseService.findShow(showId);
        validateShowBookable(show);

        List<Long> seatIds = request.getSeatIds().stream().distinct().toList();
        if (seatIds.isEmpty()) {
            throw new BusinessException(
                    "At least one seat is required",
                    HttpStatus.BAD_REQUEST,
                    "SEATS_REQUIRED"
            );
        }

        List<ShowSeat> allocations = showSeatRepository.lockByShowIdAndSeatIds(showId, seatIds);
        if (allocations.size() != seatIds.size()) {
            throw new BusinessException(
                    "One or more seats are not allocated to this show",
                    HttpStatus.BAD_REQUEST,
                    "SEAT_NOT_ALLOCATED"
            );
        }

        seatAvailabilityService.validateSeatsAvailable(showId, seatIds);

        List<Seat> seats = allocations.stream().map(ShowSeat::getSeat).toList();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Instant expiresAt = Instant.now().plusSeconds(bookingProperties.hold().durationMinutes() * 60L);

        SeatHold hold = SeatHold.builder()
                .show(show)
                .user(user)
                .status(HoldStatus.ACTIVE)
                .expiresAt(expiresAt)
                .build();

        for (Seat seat : seats) {
            SeatHoldItem item = SeatHoldItem.builder()
                    .show(show)
                    .seat(seat)
                    .build();
            hold.addItem(item);
        }

        return toResponse(seatHoldRepository.save(hold));
    }

    @Transactional(readOnly = true)
    public HoldResponse getHold(Long holdId, Long userId) {
        SeatHold hold = seatHoldRepository.findByIdAndUserId(holdId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Hold", holdId));
        return toResponse(hold);
    }

    @Transactional
    public void cancelHold(Long holdId, Long userId) {
        SeatHold hold = seatHoldRepository.findByIdAndUserId(holdId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Hold", holdId));

        if (hold.getStatus() != HoldStatus.ACTIVE) {
            throw new BusinessException(
                    "Only active holds can be cancelled",
                    HttpStatus.BAD_REQUEST,
                    "HOLD_NOT_ACTIVE"
            );
        }

        hold.setStatus(HoldStatus.CANCELLED);
        seatHoldRepository.save(hold);
    }

    private void validateShowBookable(Show show) {
        if (show.getStatus() != ShowStatus.SCHEDULED) {
            throw new BusinessException(
                    "Show is not available for booking",
                    HttpStatus.BAD_REQUEST,
                    "SHOW_NOT_AVAILABLE"
            );
        }
        if (!show.getStartTime().isAfter(Instant.now())) {
            throw new BusinessException(
                    "Cannot hold seats for a show that has already started",
                    HttpStatus.BAD_REQUEST,
                    "SHOW_ALREADY_STARTED"
            );
        }
    }

    private HoldResponse toResponse(SeatHold hold) {
        long expiresInSeconds = Math.max(0, Duration.between(Instant.now(), hold.getExpiresAt()).getSeconds());

        List<HoldSeatResponse> seats = hold.getItems().stream()
                .map(item -> HoldSeatResponse.builder()
                        .seatId(item.getSeat().getId())
                        .rowLabel(item.getSeat().getRowLabel())
                        .seatNumber(item.getSeat().getSeatNumber())
                        .category(item.getSeat().getCategory())
                        .build())
                .toList();

        return HoldResponse.builder()
                .id(hold.getId())
                .showId(hold.getShow().getId())
                .movieTitle(hold.getShow().getMovieTitle())
                .status(hold.getStatus())
                .expiresAt(hold.getExpiresAt())
                .expiresInSeconds(expiresInSeconds)
                .seats(seats)
                .build();
    }
}
