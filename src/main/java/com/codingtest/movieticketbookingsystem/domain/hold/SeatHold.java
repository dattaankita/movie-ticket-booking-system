package com.codingtest.movieticketbookingsystem.domain.hold;

import com.codingtest.movieticketbookingsystem.common.enums.HoldStatus;
import com.codingtest.movieticketbookingsystem.domain.show.Show;
import com.codingtest.movieticketbookingsystem.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seat_holds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatHold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HoldStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @OneToMany(mappedBy = "hold", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SeatHoldItem> items = new ArrayList<>();

    @Version
    @Column(nullable = false)
    private Long version;

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
            status = HoldStatus.ACTIVE;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void addItem(SeatHoldItem item) {
        items.add(item);
        item.setHold(this);
    }
}
