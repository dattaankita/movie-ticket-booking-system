package com.codingtest.movieticketbookingsystem.repository;

import com.codingtest.movieticketbookingsystem.common.enums.BookingStatus;
import com.codingtest.movieticketbookingsystem.domain.booking.Booking;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Booking> findByHold_Id(Long holdId);

    Optional<Booking> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

    Optional<Booking> findByHold_IdAndStatus(Long holdId, BookingStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :id AND b.user.id = :userId")
    Optional<Booking> findByIdAndUserIdForUpdate(@Param("id") Long id, @Param("userId") Long userId);

    List<Booking> findByStatusAndShow_StartTimeBetween(
            BookingStatus status,
            Instant startTime,
            Instant endTime
    );

    boolean existsByShow_Id(Long showId);
}
