package com.codingtest.movieticketbookingsystem.dto.admin.city;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityResponse {

    private Long id;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;
}
