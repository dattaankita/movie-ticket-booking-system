package com.codingtest.movieticketbookingsystem.dto.admin.seat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatLayoutRequest {

    @NotEmpty(message = "At least one seat is required")
    @Valid
    private List<SeatRequest> seats;
}
