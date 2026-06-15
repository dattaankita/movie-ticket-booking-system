package com.codingtest.movieticketbookingsystem.dto.hold;

import com.codingtest.movieticketbookingsystem.common.enums.SeatCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldSeatResponse {

    private Long seatId;
    private String rowLabel;
    private Integer seatNumber;
    private SeatCategory category;
}
