package com.codingtest.movieticketbookingsystem.repository;

import com.codingtest.movieticketbookingsystem.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id IN :seatIds AND s.eventId = :eventId")
    List<Seat> lockSeats(@Param("seatIds") List<Long> seatIds,
                         @Param("eventId") Long eventId);

    long countByEventIdAndBookedTrue(Long eventId);
    long countByEventIdAndHeldTrue(Long eventId);
}
