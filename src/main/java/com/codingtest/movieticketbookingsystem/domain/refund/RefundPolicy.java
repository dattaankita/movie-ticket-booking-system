package com.codingtest.movieticketbookingsystem.domain.refund;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "refund_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "hours_before_show", nullable = false)
    private Integer hoursBeforeShow;

    @Column(name = "refund_percentage", nullable = false)
    private BigDecimal refundPercentage;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
