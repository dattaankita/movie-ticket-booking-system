package com.codingtest.movieticketbookingsystem.service;

import com.codingtest.movieticketbookingsystem.entity.Booking;
import com.codingtest.movieticketbookingsystem.entity.Event;
import com.codingtest.movieticketbookingsystem.entity.Hold;
import com.codingtest.movieticketbookingsystem.entity.Seat;
import com.codingtest.movieticketbookingsystem.repository.BookingRepository;
import com.codingtest.movieticketbookingsystem.repository.EventRepository;
import com.codingtest.movieticketbookingsystem.repository.HoldRepository;
import com.codingtest.movieticketbookingsystem.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService{

    @Autowired
    private SeatRepository seatRepo;
    @Autowired
    private HoldRepository holdRepo;
    @Autowired
    private BookingRepository bookingRepo;
    @Autowired
    private EventRepository eventRepo;

    // Helper
    private String toStringIds(List<Long> ids) {
        return ids.stream().map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private List<Long> parseIds(String ids) {
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    // HOLD SEATS
    @Transactional
    public String holdSeats(Long eventId, Long userId, List<Long> seatIds) {

        Event event = eventRepo.findById(eventId).orElseThrow();

        long used = seatRepo.countByEventIdAndBookedTrue(eventId)
                + seatRepo.countByEventIdAndHeldTrue(eventId);

        if (used + seatIds.size() > event.getTotalSeats()) {
            throw new RuntimeException("Not enough seats");
        }

        List<Seat> seats = seatRepo.lockSeats(seatIds, eventId);

        for (Seat s : seats) {
            if (s.isBooked() || s.isHeld()) {
                throw new RuntimeException("Seat not available");
            }
            s.setHeld(true);
        }

        String holdId = UUID.randomUUID().toString();

        Hold hold = new Hold();
        hold.setHoldId(holdId);
        hold.setEventId(eventId);
        hold.setUserId(userId);
        hold.setSeatIds(toStringIds(seatIds));
        hold.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        holdRepo.save(hold);
        seatRepo.saveAll(seats);

        return holdId;
    }

    // CONFIRM BOOKING
    @Transactional
    public void confirmBooking(String holdId) {

        Hold hold = holdRepo.findById(holdId).orElseThrow();

        if (hold.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Hold expired");
        }

        if (bookingRepo.existsByUserIdAndEventIdAndStatus(
                hold.getUserId(), hold.getEventId(), "CONFIRMED")) {
            throw new RuntimeException("Already booked");
        }

        List<Long> seatIds = parseIds(hold.getSeatIds());
        List<Seat> seats = seatRepo.lockSeats(seatIds, hold.getEventId());

        for (Seat s : seats) {
            s.setHeld(false);
            s.setBooked(true);
        }

        Booking b = new Booking();
        b.setEventId(hold.getEventId());
        b.setUserId(hold.getUserId());
        b.setSeatIds(hold.getSeatIds());
        b.setStatus("CONFIRMED");

        bookingRepo.save(b);
        seatRepo.saveAll(seats);
        holdRepo.delete(hold);
    }

    // CANCEL BOOKING
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking b = bookingRepo.findById(bookingId).orElseThrow();

        List<Long> seatIds = parseIds(b.getSeatIds());
        List<Seat> seats = seatRepo.lockSeats(seatIds, b.getEventId());

        for (Seat s : seats) {
            s.setBooked(false);
        }

        b.setStatus("CANCELLED");
        seatRepo.saveAll(seats);
        bookingRepo.save(b);
    }
}