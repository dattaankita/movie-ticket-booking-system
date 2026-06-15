package com.codingtest.movieticketbookingsystem;

import com.codingtest.movieticketbookingsystem.common.enums.HoldStatus;
import com.codingtest.movieticketbookingsystem.config.BookingProperties;
import com.codingtest.movieticketbookingsystem.domain.hold.SeatHold;
import com.codingtest.movieticketbookingsystem.repository.SeatHoldRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoldExpiryServiceTest {

    @Mock
    private SeatHoldRepository seatHoldRepository;

    @Mock
    private BookingProperties bookingProperties;

    @InjectMocks
    private HoldExpiryService holdExpiryService;

    @Test
    void expireHoldsMarksActiveExpiredHoldsInBatches() {
        SeatHold hold = SeatHold.builder()
                .id(1L)
                .status(HoldStatus.ACTIVE)
                .expiresAt(Instant.now().minusSeconds(60))
                .build();

        when(bookingProperties.hold()).thenReturn(new BookingProperties.Hold(5, 30000, 500));
        when(seatHoldRepository.findExpiredActiveHoldsForUpdate(eq(HoldStatus.ACTIVE), any(Instant.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(hold)))
                .thenReturn(Page.empty());

        int count = holdExpiryService.expireHolds();

        assertThat(count).isEqualTo(1);
        assertThat(hold.getStatus()).isEqualTo(HoldStatus.EXPIRED);
        verify(seatHoldRepository, times(2))
                .findExpiredActiveHoldsForUpdate(eq(HoldStatus.ACTIVE), any(Instant.class), any(Pageable.class));
    }
}
