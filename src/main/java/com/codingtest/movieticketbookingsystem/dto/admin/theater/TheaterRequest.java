package com.codingtest.movieticketbookingsystem.dto.admin.theater;

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
public class TheaterRequest {

    @NotNull(message = "City id is required")
    private Long cityId;

    @NotBlank(message = "Theater name is required")
    @Size(max = 255, message = "Theater name must be at most 255 characters")
    private String name;

    @Size(max = 1000, message = "Address must be at most 1000 characters")
    private String address;
}
