package com.codingtest.movieticketbookingsystem.domain.hold;

import com.codingtest.movieticketbookingsystem.domain.seat.Seat;
import com.codingtest.movieticketbookingsystem.domain.show.Show;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seat_hold_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatHoldItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hold_id", nullable = false)
    private SeatHold hold;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;
}
