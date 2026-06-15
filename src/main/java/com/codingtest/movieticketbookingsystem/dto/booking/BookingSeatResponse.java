package com.codingtest.movieticketbookingsystem.dto.booking;

import com.codingtest.movieticketbookingsystem.common.enums.SeatCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSeatResponse {

    private Long seatId;
    private String rowLabel;
    private Integer seatNumber;
    private SeatCategory category;
    private BigDecimal unitPrice;
}
