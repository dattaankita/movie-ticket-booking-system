package com.codingtest.movieticketbookingsystem.domain.show;

import com.codingtest.movieticketbookingsystem.domain.seat.Seat;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "show_seats",
        uniqueConstraints = @UniqueConstraint(name = "uk_show_seat", columnNames = {"show_id", "seat_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;
}
