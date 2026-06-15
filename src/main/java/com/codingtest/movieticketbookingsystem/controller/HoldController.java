package com.codingtest.movieticketbookingsystem.controller;

import com.codingtest.movieticketbookingsystem.common.dto.ApiResponse;
import com.codingtest.movieticketbookingsystem.dto.hold.CreateHoldRequest;
import com.codingtest.movieticketbookingsystem.dto.hold.HoldResponse;
import com.codingtest.movieticketbookingsystem.security.SecurityUtils;
import com.codingtest.movieticketbookingsystem.service.HoldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HoldController {

    private final HoldService holdService;

    @PostMapping("/shows/{showId}/holds")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<HoldResponse> createHold(
            @PathVariable Long showId,
            @Valid @RequestBody CreateHoldRequest request) {
        Long userId = SecurityUtils.getCurrentUser().getId();
        return ApiResponse.ok("Seats held successfully", holdService.createHold(showId, request, userId));
    }

    @GetMapping("/holds/{holdId}")
    public ApiResponse<HoldResponse> getHold(@PathVariable Long holdId) {
        Long userId = SecurityUtils.getCurrentUser().getId();
        return ApiResponse.ok(holdService.getHold(holdId, userId));
    }

    @DeleteMapping("/holds/{holdId}")
    public ApiResponse<Void> cancelHold(@PathVariable Long holdId) {
        Long userId = SecurityUtils.getCurrentUser().getId();
        holdService.cancelHold(holdId, userId);
        return ApiResponse.okMessage("Hold cancelled");
    }
}
