package com.codingtest.movieticketbookingsystem.repository;

import com.codingtest.movieticketbookingsystem.common.enums.PricingTierType;
import com.codingtest.movieticketbookingsystem.domain.pricing.PricingTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PricingTierRepository extends JpaRepository<PricingTier, Long> {

    Optional<PricingTier> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    Optional<PricingTier> findFirstByTierTypeAndActiveTrue(PricingTierType tierType);
}
