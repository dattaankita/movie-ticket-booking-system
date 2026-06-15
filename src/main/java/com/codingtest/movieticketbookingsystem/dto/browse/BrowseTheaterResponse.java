package com.codingtest.movieticketbookingsystem.dto.browse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrowseTheaterResponse {

    private Long id;
    private Long cityId;
    private String cityName;
    private String name;
    private String address;
}
