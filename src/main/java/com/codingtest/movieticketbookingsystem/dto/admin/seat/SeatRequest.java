package com.codingtest.movieticketbookingsystem.dto.admin.seat;

import com.codingtest.movieticketbookingsystem.common.enums.SeatCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatRequest {

    @NotBlank(message = "Row label is required")
    @Size(max = 5, message = "Row label must be at most 5 characters")
    private String rowLabel;

    @NotNull(message = "Seat number is required")
    @Min(value = 1, message = "Seat number must be at least 1")
    private Integer seatNumber;

    private SeatCategory category;
}
