package com.codingtest.movieticketbookingsystem.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HoldRequest {
    private Long eventId;
    private Long userId;
    private List<Long> seatIds;
}