package com.codingtest.movieticketbookingsystem.scheduler;

import com.codingtest.movieticketbookingsystem.entity.Hold;
import com.codingtest.movieticketbookingsystem.entity.Seat;
import com.codingtest.movieticketbookingsystem.repository.HoldRepository;
import com.codingtest.movieticketbookingsystem.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@EnableScheduling
class HoldExpiryScheduler {

    @Autowired
    private HoldRepository holdRepo;
    @Autowired
    private SeatRepository seatRepo;

    @Scheduled(fixedRate = 60000)
    public void cleanup() {

        for (Hold h : holdRepo.findAll()) {
            if (h.getExpiryTime().isBefore(LocalDateTime.now())) {

                List<Long> ids = Arrays.stream(h.getSeatIds().split(","))
                        .map(String::trim)
                        .map(Long::parseLong)
                        .collect(Collectors.toList());

                List<Seat> seats = seatRepo.lockSeats(ids, h.getEventId());

                for (Seat s : seats) {
                    s.setHeld(false);
                }

                seatRepo.saveAll(seats);
                holdRepo.delete(h);
            }
        }
    }
}