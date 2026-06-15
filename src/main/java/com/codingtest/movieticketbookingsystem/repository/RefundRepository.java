package com.codingtest.movieticketbookingsystem.repository;

import com.codingtest.movieticketbookingsystem.domain.refund.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    boolean existsByBooking_Id(Long bookingId);

    Optional<Refund> findByBooking_Id(Long bookingId);
}
