package com.codingtest.movieticketbookingsystem.dto.admin.city;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityRequest {

    @NotBlank(message = "City name is required")
    @Size(max = 255, message = "City name must be at most 255 characters")
    private String name;
}
