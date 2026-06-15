package com.codingtest.movieticketbookingsystem.service;

import com.codingtest.movieticketbookingsystem.common.dto.PageResponse;
import com.codingtest.movieticketbookingsystem.common.enums.BookingStatus;
import com.codingtest.movieticketbookingsystem.common.enums.HoldStatus;
import com.codingtest.movieticketbookingsystem.common.enums.PaymentStatus;
import com.codingtest.movieticketbookingsystem.common.exception.BusinessException;
import com.codingtest.movieticketbookingsystem.common.exception.ResourceNotFoundException;
import com.codingtest.movieticketbookingsystem.common.util.PaginationUtils;
import com.codingtest.movieticketbookingsystem.config.BookingProperties;
import com.codingtest.movieticketbookingsystem.domain.booking.Booking;
import com.codingtest.movieticketbookingsystem.domain.booking.BookingSeat;
import com.codingtest.movieticketbookingsystem.domain.discount.DiscountCode;
import com.codingtest.movieticketbookingsystem.domain.hold.SeatHold;
import com.codingtest.movieticketbookingsystem.domain.hold.SeatHoldItem;
import com.codingtest.movieticketbookingsystem.domain.payment.Payment;
import com.codingtest.movieticketbookingsystem.domain.seat.Seat;
import com.codingtest.movieticketbookingsystem.domain.show.Show;
import com.codingtest.movieticketbookingsystem.dto.booking.BookingConfirmationResult;
import com.codingtest.movieticketbookingsystem.dto.booking.BookingResponse;
import com.codingtest.movieticketbookingsystem.dto.booking.BookingSeatResponse;
import com.codingtest.movieticketbookingsystem.dto.booking.CreateBookingRequest;
import com.codingtest.movieticketbookingsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final SeatHoldRepository seatHoldRepository;
    private final ShowSeatRepository showSeatRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final DiscountCodeRepository discountCodeRepository;
    private final PricingService pricingService;
    private final DiscountService discountService;
    private final PaymentService paymentService;
    private final SeatAvailabilityService seatAvailabilityService;
    private final BookingProperties bookingProperties;
    private final NotificationService notificationService;

    @Transactional
    public BookingConfirmationResult confirmBooking(
            CreateBookingRequest request,
            Long userId,
            String idempotencyKey) {

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = bookingRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey.trim());
            if (existing.isPresent()) {
                Booking booking = existing.get();
                return replayResult(booking);
            }
        }

        SeatHold hold = seatHoldRepository.findByIdAndUserIdForUpdate(request.getHoldId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Hold", request.getHoldId()));

        if (hold.getStatus() == HoldStatus.CONVERTED) {
            Booking existing = bookingRepository.findByHold_Id(hold.getId())
                    .orElseThrow(() -> new BusinessException(
                            "Hold already converted but booking not found",
                            HttpStatus.CONFLICT,
                            "BOOKING_NOT_FOUND"
                    ));
            return replayResult(existing);
        }

        validateHoldForBooking(hold);

        Show show = hold.getShow();
        List<Long> seatIds = hold.getItems().stream()
                .map(item -> item.getSeat().getId())
                .toList();

        showSeatRepository.lockByShowIdAndSeatIds(show.getId(), seatIds);
        seatAvailabilityService.validateSeatsAvailable(show.getId(), seatIds, hold.getId());

        List<Seat> seats = hold.getItems().stream()
                .map(SeatHoldItem::getSeat)
                .toList();

        PricingService.PricingResult pricing = pricingService.calculate(show, seats);

        DiscountCode discountCode = discountService.lockActiveCode(request.getDiscountCode());
        BigDecimal discountAmount = discountService.calculateDiscount(discountCode, pricing.subtotal());
        BigDecimal totalAmount = pricing.subtotal()
                .subtract(discountAmount)
                .max(BigDecimal.ZERO)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        Booking booking = bookingRepository.findByHold_IdAndStatus(hold.getId(), BookingStatus.FAILED)
                .orElseGet(() -> Booking.builder()
                        .user(hold.getUser())
                        .show(show)
                        .hold(hold)
                        .status(BookingStatus.PENDING)
                        .build());

        booking.setSubtotal(pricing.subtotal());
        booking.setDiscountAmount(discountAmount);
        booking.setTotalAmount(totalAmount);
        booking.setDiscountCode(discountCode);
        booking.setStatus(BookingStatus.PENDING);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            booking.setIdempotencyKey(idempotencyKey.trim());
        }

        booking.getSeats().clear();
        for (SeatHoldItem item : hold.getItems()) {
            BookingSeat bookingSeat = BookingSeat.builder()
                    .show(show)
                    .seat(item.getSeat())
                    .unitPrice(pricing.unitPrices().get(item.getSeat().getId()))
                    .build();
            booking.addSeat(bookingSeat);
        }

        booking = bookingRepository.save(booking);

        Booking finalBooking = booking;
        Payment payment = paymentRepository.findByBooking_Id(booking.getId())
                .orElseGet(() -> Payment.builder().booking(finalBooking).build());
        Payment processed = paymentService.processPayment(booking);
        payment.setAmount(processed.getAmount());
        payment.setStatus(processed.getStatus());
        payment.setTransactionRef(processed.getTransactionRef());
        paymentRepository.save(payment);

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            booking.setStatus(BookingStatus.CONFIRMED);
            hold.setStatus(HoldStatus.CONVERTED);
            if (discountCode != null) {
                discountCode.setUsedCount(discountCode.getUsedCount() + 1);
                discountCodeRepository.save(discountCode);
            }
            bookingRepository.save(booking);
            seatHoldRepository.save(hold);
            notificationService.sendBookingConfirmation(booking.getId());
            return new BookingConfirmationResult(toResponse(booking, payment), true);
        }

        booking.setStatus(BookingStatus.FAILED);
        bookingRepository.save(booking);
        throw new BusinessException(
                "Payment failed",
                HttpStatus.PAYMENT_REQUIRED,
                "PAYMENT_FAILED"
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> listMyBookings(Long userId, Integer page, Integer size) {
        Pageable pageable = PaginationUtils.of(page, size);
        return PageResponse.from(
                bookingRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                        .map(booking -> toResponse(booking, findPayment(booking)))
        );
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .filter(b -> b.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        return toResponse(booking, findPayment(booking));
    }

    private BookingConfirmationResult replayResult(Booking booking) {
        return new BookingConfirmationResult(toResponse(booking, findPayment(booking)), false);
    }

    private void validateHoldForBooking(SeatHold hold) {
        if (hold.getStatus() != HoldStatus.ACTIVE) {
            throw new BusinessException(
                    "Hold is not active",
                    HttpStatus.BAD_REQUEST,
                    "HOLD_NOT_ACTIVE"
            );
        }

        if (!hold.getExpiresAt().isAfter(Instant.now())) {
            hold.setStatus(HoldStatus.EXPIRED);
            seatHoldRepository.save(hold);
            throw new BusinessException(
                    "Hold has expired",
                    HttpStatus.BAD_REQUEST,
                    "HOLD_EXPIRED"
            );
        }
    }

    private Payment findPayment(Booking booking) {
        return paymentRepository.findByBooking_Id(booking.getId()).orElse(null);
    }

    private BookingResponse toResponse(Booking booking, Payment payment) {
        List<BookingSeatResponse> seats = booking.getSeats().stream()
                .map(seat -> BookingSeatResponse.builder()
                        .seatId(seat.getSeat().getId())
                        .rowLabel(seat.getSeat().getRowLabel())
                        .seatNumber(seat.getSeat().getSeatNumber())
                        .category(seat.getSeat().getCategory())
                        .unitPrice(seat.getUnitPrice())
                        .build())
                .toList();

        return BookingResponse.builder()
                .id(booking.getId())
                .showId(booking.getShow().getId())
                .movieTitle(booking.getShow().getMovieTitle())
                .showStartTime(booking.getShow().getStartTime())
                .status(booking.getStatus())
                .subtotal(booking.getSubtotal())
                .discountAmount(booking.getDiscountAmount())
                .totalAmount(booking.getTotalAmount())
                .currency(bookingProperties.currency())
                .discountCode(booking.getDiscountCode() != null ? booking.getDiscountCode().getCode() : null)
                .paymentStatus(payment != null ? payment.getStatus() : null)
                .transactionRef(payment != null ? payment.getTransactionRef() : null)
                .seats(seats)
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
