package com.codingtest.movieticketbookingsystem.repository;

import com.codingtest.movieticketbookingsystem.common.enums.HoldStatus;
import com.codingtest.movieticketbookingsystem.domain.hold.SeatHoldItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SeatHoldItemRepository extends JpaRepository<SeatHoldItem, Long> {

    @Query("""
            SELECT item.seat.id
            FROM SeatHoldItem item
            JOIN item.hold hold
            WHERE item.show.id = :showId
              AND hold.status = :status
              AND hold.expiresAt > :now
            """)
    List<Long> findHeldSeatIdsForShow(
            @Param("showId") Long showId,
            @Param("status") HoldStatus status,
            @Param("now") Instant now
    );

    @Query("""
            SELECT item.seat.id
            FROM SeatHoldItem item
            JOIN item.hold hold
            WHERE item.show.id = :showId
              AND item.seat.id IN :seatIds
              AND hold.status = :status
              AND hold.expiresAt > :now
            """)
    List<Long> findHeldSeatIdsForShowAndSeats(
            @Param("showId") Long showId,
            @Param("seatIds") List<Long> seatIds,
            @Param("status") HoldStatus status,
            @Param("now") Instant now
    );

    @Query("""
            SELECT item.seat.id
            FROM SeatHoldItem item
            JOIN item.hold hold
            WHERE item.show.id = :showId
              AND item.seat.id IN :seatIds
              AND hold.status = :status
              AND hold.expiresAt > :now
              AND hold.id <> :excludeHoldId
            """)
    List<Long> findHeldSeatIdsForShowAndSeatsExcludingHold(
            @Param("showId") Long showId,
            @Param("seatIds") List<Long> seatIds,
            @Param("status") HoldStatus status,
            @Param("now") Instant now,
            @Param("excludeHoldId") Long excludeHoldId
    );

    @Query("""
            SELECT CASE WHEN COUNT(item) > 0 THEN true ELSE false END
            FROM SeatHoldItem item
            JOIN item.hold hold
            WHERE item.seat.id = :seatId
              AND hold.status = :status
              AND hold.expiresAt > :now
            """)
    boolean existsActiveHoldForSeatId(
            @Param("seatId") Long seatId,
            @Param("status") HoldStatus status,
            @Param("now") Instant now
    );
}
