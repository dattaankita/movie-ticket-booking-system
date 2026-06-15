package com.codingtest.movieticketbookingsystem;

import com.codingtest.movieticketbookingsystem.common.enums.BookingStatus;
import com.codingtest.movieticketbookingsystem.common.enums.HoldStatus;
import com.codingtest.movieticketbookingsystem.common.enums.PaymentStatus;
import com.codingtest.movieticketbookingsystem.common.exception.BusinessException;
import com.codingtest.movieticketbookingsystem.config.BookingProperties;
import com.codingtest.movieticketbookingsystem.domain.booking.Booking;
import com.codingtest.movieticketbookingsystem.domain.hold.SeatHold;
import com.codingtest.movieticketbookingsystem.domain.hold.SeatHoldItem;
import com.codingtest.movieticketbookingsystem.domain.payment.Payment;
import com.codingtest.movieticketbookingsystem.domain.seat.Seat;
import com.codingtest.movieticketbookingsystem.domain.show.Show;
import com.codingtest.movieticketbookingsystem.domain.show.ShowSeat;
import com.codingtest.movieticketbookingsystem.domain.theater.Theater;
import com.codingtest.movieticketbookingsystem.domain.user.User;
import com.codingtest.movieticketbookingsystem.dto.booking.CreateBookingRequest;
import com.codingtest.movieticketbookingsystem.repository.*;
import com.codingtest.movieticketbookingsystem.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private SeatHoldRepository seatHoldRepository;

    @Mock
    private ShowSeatRepository showSeatRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private DiscountCodeRepository discountCodeRepository;

    @Mock
    private PricingService pricingService;

    @Mock
    private DiscountService discountService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private SeatAvailabilityService seatAvailabilityService;

    @Mock
    private BookingProperties bookingProperties;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookingService bookingService;

    private SeatHold hold;
    private User user;
    private Show show;
    private Seat seat;

    @BeforeEach
    void setUp() {
        Theater theater = Theater.builder().id(1L).build();
        user = User.builder().id(5L).build();
        show = Show.builder().id(10L).theater(theater).basePrice(new BigDecimal("300.00")).build();
        seat = Seat.builder().id(100L).theater(theater).build();

        SeatHoldItem item = SeatHoldItem.builder().show(show).seat(seat).build();
        hold = SeatHold.builder()
                .id(1L)
                .show(show)
                .user(user)
                .status(HoldStatus.ACTIVE)
                .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                .build();
        hold.addItem(item);

        when(bookingProperties.currency()).thenReturn("INR");
    }

    @Test
    void confirmBookingRejectsExpiredHold() {
        hold.setExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES));
        when(seatHoldRepository.findByIdAndUserIdForUpdate(1L, 5L)).thenReturn(Optional.of(hold));
        when(seatHoldRepository.save(hold)).thenReturn(hold);

        CreateBookingRequest request = CreateBookingRequest.builder().holdId(1L).build();

        assertThatThrownBy(() -> bookingService.confirmBooking(request, 5L, null))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo("HOLD_EXPIRED"));
    }

    @Test
    void confirmBookingExcludesConvertingHoldFromAvailabilityCheck() {
        stubSuccessfulBookingPath();

        CreateBookingRequest request = CreateBookingRequest.builder().holdId(1L).build();
        bookingService.confirmBooking(request, 5L, null);

        verify(seatAvailabilityService).validateSeatsAvailable(eq(10L), eq(List.of(100L)), eq(1L));
    }

    @Test
    void confirmBookingReusesFailedBookingOnPaymentRetry() {
        when(seatHoldRepository.findByIdAndUserIdForUpdate(1L, 5L)).thenReturn(Optional.of(hold));
        when(showSeatRepository.lockByShowIdAndSeatIds(10L, List.of(100L)))
                .thenReturn(List.of(ShowSeat.builder().show(show).seat(seat).build()));
        when(discountService.lockActiveCode(null)).thenReturn(null);
        when(discountService.calculateDiscount(null, new BigDecimal("450.00"))).thenReturn(BigDecimal.ZERO);
        when(pricingService.calculate(show, List.of(seat)))
                .thenReturn(new PricingService.PricingResult(
                        java.util.Map.of(100L, new BigDecimal("450.00")),
                        new BigDecimal("450.00")
                ));

        Booking failedBooking = Booking.builder()
                .id(99L)
                .user(user)
                .show(show)
                .hold(hold)
                .status(BookingStatus.FAILED)
                .build();

        Payment existingPayment = Payment.builder().booking(failedBooking).build();
        Payment successPayment = Payment.builder()
                .amount(new BigDecimal("450.00"))
                .status(PaymentStatus.SUCCESS)
                .transactionRef("txn-retry")
                .build();

        when(bookingRepository.findByHold_IdAndStatus(1L, BookingStatus.FAILED))
                .thenReturn(Optional.of(failedBooking));
        when(bookingRepository.save(failedBooking)).thenReturn(failedBooking);
        when(paymentRepository.findByBooking_Id(99L)).thenReturn(Optional.of(existingPayment));
        when(paymentService.processPayment(failedBooking)).thenReturn(successPayment);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(seatHoldRepository.save(hold)).thenReturn(hold);

        CreateBookingRequest request = CreateBookingRequest.builder().holdId(1L).build();
        var result = bookingService.confirmBooking(request, 5L, null);

        assertThat(result.created()).isTrue();
        assertThat(result.response().getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(notificationService).sendBookingConfirmation(99L);
    }

    private void stubSuccessfulBookingPath() {
        when(seatHoldRepository.findByIdAndUserIdForUpdate(1L, 5L)).thenReturn(Optional.of(hold));
        when(showSeatRepository.lockByShowIdAndSeatIds(10L, List.of(100L)))
                .thenReturn(List.of(ShowSeat.builder().show(show).seat(seat).build()));
        when(discountService.lockActiveCode(null)).thenReturn(null);
        when(discountService.calculateDiscount(null, new BigDecimal("450.00"))).thenReturn(BigDecimal.ZERO);
        when(pricingService.calculate(show, List.of(seat)))
                .thenReturn(new PricingService.PricingResult(
                        java.util.Map.of(100L, new BigDecimal("450.00")),
                        new BigDecimal("450.00")
                ));

        Booking pendingBooking = Booking.builder()
                .id(99L)
                .user(user)
                .show(show)
                .hold(hold)
                .status(BookingStatus.PENDING)
                .build();

        when(bookingRepository.findByHold_IdAndStatus(1L, BookingStatus.FAILED)).thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenReturn(pendingBooking);
        when(paymentRepository.findByBooking_Id(99L)).thenReturn(Optional.empty());
        when(paymentService.processPayment(any(Booking.class))).thenReturn(Payment.builder()
                .amount(new BigDecimal("450.00"))
                .status(PaymentStatus.SUCCESS)
                .transactionRef("txn-1")
                .build());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(seatHoldRepository.save(hold)).thenReturn(hold);
        when(bookingRepository.save(pendingBooking)).thenReturn(pendingBooking);
    }
}
