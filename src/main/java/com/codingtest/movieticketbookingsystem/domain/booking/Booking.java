package com.codingtest.movieticketbookingsystem.domain.booking;

import com.codingtest.movieticketbookingsystem.common.enums.BookingStatus;
import com.codingtest.movieticketbookingsystem.domain.discount.DiscountCode;
import com.codingtest.movieticketbookingsystem.domain.hold.SeatHold;
import com.codingtest.movieticketbookingsystem.domain.show.Show;
import com.codingtest.movieticketbookingsystem.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hold_id")
    private SeatHold hold;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_code_id")
    private DiscountCode discountCode;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BookingSeat> seats = new ArrayList<>();

    @Column(name = "idempotency_key", length = 64)
    private String idempotencyKey;

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
        if (discountAmount == null) {
            discountAmount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void addSeat(BookingSeat seat) {
        seats.add(seat);
        seat.setBooking(this);
    }
}
