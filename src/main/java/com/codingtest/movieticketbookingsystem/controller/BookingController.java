package com.codingtest.movieticketbookingsystem.controller;

import com.codingtest.movieticketbookingsystem.entity.Booking;
import com.codingtest.movieticketbookingsystem.entity.Event;
import com.codingtest.movieticketbookingsystem.repository.BookingRepository;
import com.codingtest.movieticketbookingsystem.repository.EventRepository;
import com.codingtest.movieticketbookingsystem.repository.SeatRepository;
import com.codingtest.movieticketbookingsystem.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
class BookingController {

    @Autowired
    private BookingService service;
    @Autowired
    private SeatRepository seatRepo;
    @Autowired
    private EventRepository eventRepo;
    @Autowired
    private BookingRepository bookingRepo;

    @PostMapping("/holds")
    public String hold(@RequestBody Map<String, Object> req) {
        return service.holdSeats(Long.valueOf(req.get("eventId").toString()),
                Long.valueOf(req.get("userId").toString()),
                (List<Long>) req.get("seatIds"));
    }

    @PostMapping("/bookings/confirm")
    public String confirm(@RequestParam String holdId) {
        service.confirmBooking(holdId);
        return "CONFIRMED";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancel(@PathVariable Long id) {
        service.cancelBooking(id);
        return "CANCELLED";
    }

    @GetMapping("/bookings/{id}")
    public Booking view(@PathVariable Long id) {
        return bookingRepo.findById(id).orElseThrow();
    }

    // AVAILABILITY
    @GetMapping("/events/{eventId}/availability")
    public Map<String, Object> availability(@PathVariable Long eventId) {

        Event e = eventRepo.findById(eventId).orElseThrow();

        long booked = seatRepo.countByEventIdAndBookedTrue(eventId);
        long held = seatRepo.countByEventIdAndHeldTrue(eventId);

        long available = e.getTotalSeats() - (booked + held);

        return Map.of("eventId", eventId,
                "totalSeats", e.getTotalSeats(),
                "booked", booked,
                "held", held,
                "available", available);
    }
}