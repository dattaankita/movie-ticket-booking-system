package com.codingtest.movieticketbookingsystem.repository;

import com.codingtest.movieticketbookingsystem.common.enums.BookingStatus;
import com.codingtest.movieticketbookingsystem.domain.booking.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    @Query("""
            SELECT bs.seat.id
            FROM BookingSeat bs
            JOIN bs.booking b
            WHERE bs.show.id = :showId
              AND bs.seat.id IN :seatIds
              AND b.status = :status
            """)
    List<Long> findBookedSeatIdsForShow(
            @Param("showId") Long showId,
            @Param("seatIds") List<Long> seatIds,
            @Param("status") BookingStatus status
    );

    boolean existsBySeat_IdAndBooking_Status(Long seatId, BookingStatus status);
}
