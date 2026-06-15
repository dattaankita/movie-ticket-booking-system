package com.codingtest.movieticketbookingsystem.dto.admin.theater;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TheaterResponse {

    private Long id;
    private Long cityId;
    private String cityName;
    private String name;
    private String address;
    private Instant createdAt;
    private Instant updatedAt;
}
