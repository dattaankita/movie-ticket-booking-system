package com.codingtest.movieticketbookingsystem.service;

import java.util.List;

public interface BookingService {
    void confirmBooking(String holdId);
    String holdSeats(Long eventId, Long userId, List<Long> seatIds);

    void cancelBooking(Long id);
}
