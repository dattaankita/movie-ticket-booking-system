package com.codingtest.movieticketbookingsystem.dto.browse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrowseCityResponse {

    private Long id;
    private String name;
}
