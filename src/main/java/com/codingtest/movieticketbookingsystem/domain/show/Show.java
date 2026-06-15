package com.codingtest.movieticketbookingsystem.domain.show;

import com.codingtest.movieticketbookingsystem.common.enums.ShowStatus;
import com.codingtest.movieticketbookingsystem.domain.pricing.PricingTier;
import com.codingtest.movieticketbookingsystem.domain.theater.Theater;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "shows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "theater_id", nullable = false)
    private Theater theater;

    @Column(name = "movie_title", nullable = false)
    private String movieTitle;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pricing_tier_id")
    private PricingTier pricingTier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShowStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = ShowStatus.SCHEDULED;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
