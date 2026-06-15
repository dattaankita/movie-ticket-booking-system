package com.codingtest.movieticketbookingsystem.repository;

import com.codingtest.movieticketbookingsystem.domain.refund.RefundPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundPolicyRepository extends JpaRepository<RefundPolicy, Long> {

    List<RefundPolicy> findByActiveTrueOrderByHoursBeforeShowDesc();
}
