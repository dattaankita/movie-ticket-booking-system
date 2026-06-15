package com.codingtest.movieticketbookingsystem.dto.admin.seat;

import com.codingtest.movieticketbookingsystem.common.enums.SeatCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {

    private Long id;
    private Long theaterId;
    private String rowLabel;
    private Integer seatNumber;
    private SeatCategory category;
    private Instant createdAt;
    private Instant updatedAt;
}
