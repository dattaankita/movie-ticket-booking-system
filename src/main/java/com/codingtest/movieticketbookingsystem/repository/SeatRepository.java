package com.codingtest.movieticketbookingsystem.repository;

import com.codingtest.movieticketbookingsystem.domain.seat.Seat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByTheaterIdOrderByRowLabelAscSeatNumberAsc(Long theaterId);

    Page<Seat> findByTheaterIdOrderByRowLabelAscSeatNumberAsc(Long theaterId, Pageable pageable);

    boolean existsByTheaterIdAndRowLabelIgnoreCaseAndSeatNumber(Long theaterId, String rowLabel, Integer seatNumber);

    boolean existsByTheaterId(Long theaterId);
}
