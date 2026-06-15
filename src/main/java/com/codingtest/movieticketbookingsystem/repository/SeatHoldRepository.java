package com.codingtest.movieticketbookingsystem.repository;

import com.codingtest.movieticketbookingsystem.common.enums.HoldStatus;
import com.codingtest.movieticketbookingsystem.domain.hold.SeatHold;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {

    Page<SeatHold> findByStatusAndExpiresAtBefore(HoldStatus status, Instant expiresAt, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT h FROM SeatHold h
            WHERE h.status = :status
              AND h.expiresAt < :expiresAt
            """)
    Page<SeatHold> findExpiredActiveHoldsForUpdate(
            @Param("status") HoldStatus status,
            @Param("expiresAt") Instant expiresAt,
            Pageable pageable);

    Optional<SeatHold> findByIdAndUserId(Long id, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"show", "show.theater", "user", "items", "items.seat"})
    @Query("SELECT h FROM SeatHold h WHERE h.id = :id AND h.user.id = :userId")
    Optional<SeatHold> findByIdAndUserIdForUpdate(@Param("id") Long id, @Param("userId") Long userId);

    @Query("""
            SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END
            FROM SeatHold h
            JOIN h.items item
            WHERE h.show.id = :showId
              AND item.seat.id = :seatId
              AND h.status = :status
              AND h.expiresAt > :now
            """)
    boolean existsActiveHoldForSeat(
            @Param("showId") Long showId,
            @Param("seatId") Long seatId,
            @Param("status") HoldStatus status,
            @Param("now") Instant now
    );

    boolean existsByShow_Id(Long showId);
}
