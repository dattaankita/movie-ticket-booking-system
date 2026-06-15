package com.codingtest.movieticketbookingsystem.controller;

import com.codingtest.movieticketbookingsystem.common.dto.ApiResponse;
import com.codingtest.movieticketbookingsystem.common.dto.PageResponse;
import com.codingtest.movieticketbookingsystem.dto.booking.*;
import com.codingtest.movieticketbookingsystem.security.SecurityUtils;
import com.codingtest.movieticketbookingsystem.service.BookingService;
import com.codingtest.movieticketbookingsystem.service.CancellationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final BookingService bookingService;
    private final CancellationService cancellationService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @RequestHeader(value = IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey) {
        Long userId = SecurityUtils.getCurrentUser().getId();
        BookingConfirmationResult result = bookingService.confirmBooking(request, userId, idempotencyKey);

        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        String message = result.created() ? "Booking confirmed" : "Booking already confirmed";
        return ResponseEntity.status(status).body(ApiResponse.ok(message, result.response()));
    }

    @GetMapping("/me")
    public ApiResponse<PageResponse<BookingResponse>> myBookings(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        Long userId = SecurityUtils.getCurrentUser().getId();
        return ApiResponse.ok(bookingService.listMyBookings(userId, page, size));
    }

    @GetMapping("/{bookingId}")
    public ApiResponse<BookingResponse> getBooking(@PathVariable Long bookingId) {
        Long userId = SecurityUtils.getCurrentUser().getId();
        return ApiResponse.ok(bookingService.getBooking(bookingId, userId));
    }

    @PostMapping("/{bookingId}/cancel")
    public ApiResponse<CancelBookingResponse> cancelBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody(required = false) CancelBookingRequest request) {
        Long userId = SecurityUtils.getCurrentUser().getId();
        return ApiResponse.ok("Booking cancelled", cancellationService.cancelBooking(bookingId, userId, request));
    }
}
