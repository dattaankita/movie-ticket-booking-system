package com.codingtest.movieticketbookingsystem.dto.hold;

import com.codingtest.movieticketbookingsystem.common.enums.HoldStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldResponse {

    private Long id;
    private Long showId;
    private String movieTitle;
    private HoldStatus status;
    private Instant expiresAt;
    private long expiresInSeconds;
    private List<HoldSeatResponse> seats;
}
