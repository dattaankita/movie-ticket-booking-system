package com.codingtest.movieticketbookingsystem.controller;

import com.codingtest.movieticketbookingsystem.common.dto.ApiResponse;
import com.codingtest.movieticketbookingsystem.common.dto.PageResponse;
import com.codingtest.movieticketbookingsystem.dto.browse.BrowseCityResponse;
import com.codingtest.movieticketbookingsystem.dto.browse.BrowseSeatResponse;
import com.codingtest.movieticketbookingsystem.dto.browse.BrowseShowResponse;
import com.codingtest.movieticketbookingsystem.dto.browse.BrowseTheaterResponse;
import com.codingtest.movieticketbookingsystem.service.BrowseService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/browse")
@RequiredArgsConstructor
public class BrowseController {

    private final BrowseService browseService;

    @GetMapping("/cities")
    public ApiResponse<PageResponse<BrowseCityResponse>> listCities(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.ok(browseService.listCities(page, size));
    }

    @GetMapping("/cities/{cityId}/theaters")
    public ApiResponse<PageResponse<BrowseTheaterResponse>> listTheaters(
            @PathVariable Long cityId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.ok(browseService.listTheatersByCity(cityId, page, size));
    }

    @GetMapping("/shows")
    public ApiResponse<PageResponse<BrowseShowResponse>> listShows(
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.ok(browseService.listShows(cityId, date, page, size));
    }

    @GetMapping("/shows/{showId}")
    public ApiResponse<BrowseShowResponse> getShow(@PathVariable Long showId) {
        return ApiResponse.ok(browseService.getShow(showId));
    }

    @GetMapping("/shows/{showId}/seats")
    public ApiResponse<PageResponse<BrowseSeatResponse>> listSeats(
            @PathVariable Long showId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.ok(browseService.listSeatsForShow(showId, page, size));
    }
}
