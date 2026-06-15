package com.codingtest.movieticketbookingsystem.dto.browse;

import com.codingtest.movieticketbookingsystem.common.enums.SeatAvailabilityStatus;
import com.codingtest.movieticketbookingsystem.common.enums.SeatCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrowseSeatResponse {

    private Long id;
    private String rowLabel;
    private Integer seatNumber;
    private SeatCategory category;
    private SeatAvailabilityStatus status;
}
