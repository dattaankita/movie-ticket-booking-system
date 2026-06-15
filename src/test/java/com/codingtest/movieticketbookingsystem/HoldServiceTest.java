package com.codingtest.movieticketbookingsystem;

import com.codingtest.movieticketbookingsystem.common.enums.HoldStatus;
import com.codingtest.movieticketbookingsystem.common.enums.ShowStatus;
import com.codingtest.movieticketbookingsystem.common.exception.BusinessException;
import com.codingtest.movieticketbookingsystem.config.BookingProperties;
import com.codingtest.movieticketbookingsystem.domain.hold.SeatHold;
import com.codingtest.movieticketbookingsystem.domain.seat.Seat;
import com.codingtest.movieticketbookingsystem.domain.show.Show;
import com.codingtest.movieticketbookingsystem.domain.show.ShowSeat;
import com.codingtest.movieticketbookingsystem.domain.theater.Theater;
import com.codingtest.movieticketbookingsystem.domain.user.User;
import com.codingtest.movieticketbookingsystem.dto.hold.CreateHoldRequest;
import com.codingtest.movieticketbookingsystem.repository.SeatHoldRepository;
import com.codingtest.movieticketbookingsystem.repository.ShowSeatRepository;
import com.codingtest.movieticketbookingsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HoldServiceTest {

    @Mock
    private SeatHoldRepository seatHoldRepository;

    @Mock
    private ShowSeatRepository showSeatRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BrowseService browseService;

    @Mock
    private SeatAvailabilityService seatAvailabilityService;

    @Mock
    private BookingProperties bookingProperties;

    @InjectMocks
    private HoldService holdService;

    private Show show;
    private Seat seat;
    private User user;

    @BeforeEach
    void setUp() {
        Theater theater = Theater.builder().id(1L).build();
        show = Show.builder()
                .id(10L)
                .theater(theater)
                .status(ShowStatus.SCHEDULED)
                .startTime(Instant.now().plus(1, ChronoUnit.DAYS))
                .build();
        seat = Seat.builder().id(100L).theater(theater).build();
        user = User.builder().id(5L).build();

        when(bookingProperties.hold()).thenReturn(new BookingProperties.Hold(5, 30000L, 500));
    }

    @Test
    void createHoldRejectsEmptySeatList() {
        when(browseService.findShow(10L)).thenReturn(show);

        assertThatThrownBy(() -> holdService.createHold(
                10L, CreateHoldRequest.builder().seatIds(List.of()).build(), 5L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> org.assertj.core.api.Assertions.assertThat(
                        ((BusinessException) ex).getErrorCode()).isEqualTo("SEATS_REQUIRED"));
    }

    @Test
    void createHoldRejectsShowThatAlreadyStarted() {
        show.setStartTime(Instant.now().minus(1, ChronoUnit.HOURS));
        when(browseService.findShow(10L)).thenReturn(show);

        assertThatThrownBy(() -> holdService.createHold(
                10L, CreateHoldRequest.builder().seatIds(List.of(100L)).build(), 5L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> org.assertj.core.api.Assertions.assertThat(
                        ((BusinessException) ex).getErrorCode()).isEqualTo("SHOW_ALREADY_STARTED"));
    }

    @Test
    void createHoldRejectsNonScheduledShow() {
        show.setStatus(ShowStatus.CANCELLED);
        when(browseService.findShow(10L)).thenReturn(show);

        assertThatThrownBy(() -> holdService.createHold(
                10L, CreateHoldRequest.builder().seatIds(List.of(100L)).build(), 5L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> org.assertj.core.api.Assertions.assertThat(
                        ((BusinessException) ex).getErrorCode()).isEqualTo("SHOW_NOT_AVAILABLE"));
    }

    @Test
    void createHoldRejectsUnallocatedSeat() {
        when(browseService.findShow(10L)).thenReturn(show);
        when(showSeatRepository.lockByShowIdAndSeatIds(10L, List.of(999L))).thenReturn(List.of());

        assertThatThrownBy(() -> holdService.createHold(
                10L, CreateHoldRequest.builder().seatIds(List.of(999L)).build(), 5L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> org.assertj.core.api.Assertions.assertThat(
                        ((BusinessException) ex).getErrorCode()).isEqualTo("SEAT_NOT_ALLOCATED"));
    }

    @Test
    void cancelHoldRejectsNonActiveHold() {
        SeatHold hold = SeatHold.builder()
                .id(1L)
                .status(HoldStatus.EXPIRED)
                .user(user)
                .build();

        when(seatHoldRepository.findByIdAndUserId(1L, 5L)).thenReturn(Optional.of(hold));

        assertThatThrownBy(() -> holdService.cancelHold(1L, 5L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> org.assertj.core.api.Assertions.assertThat(
                        ((BusinessException) ex).getErrorCode()).isEqualTo("HOLD_NOT_ACTIVE"));
    }

    @Test
    void createHoldLocksShowSeatAllocationsAndValidatesAvailability() {
        ShowSeat allocation = ShowSeat.builder().show(show).seat(seat).build();

        when(browseService.findShow(10L)).thenReturn(show);
        when(showSeatRepository.lockByShowIdAndSeatIds(10L, List.of(100L))).thenReturn(List.of(allocation));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(seatHoldRepository.save(org.mockito.ArgumentMatchers.any(SeatHold.class)))
                .thenAnswer(invocation -> {
                    SeatHold saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        holdService.createHold(10L, CreateHoldRequest.builder().seatIds(List.of(100L)).build(), 5L);

        verify(showSeatRepository).lockByShowIdAndSeatIds(10L, List.of(100L));
        verify(seatAvailabilityService).validateSeatsAvailable(eq(10L), anyList());
    }
}
