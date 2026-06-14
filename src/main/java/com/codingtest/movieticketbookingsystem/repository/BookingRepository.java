package com.codingtest.movieticketbookingsystem.repository;

import com.codingtest.movieticketbookingsystem.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByUserIdAndEventIdAndStatus(Long userId, Long eventId, String status);
}
