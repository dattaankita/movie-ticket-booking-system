package com.codingtest.movieticketbookingsystem.dto.hold;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateHoldRequest {

    @NotEmpty(message = "At least one seat id is required")
    private List<@NotNull(message = "Seat id cannot be null") Long> seatIds;
}
