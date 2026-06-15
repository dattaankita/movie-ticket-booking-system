package com.codingtest.movieticketbookingsystem.service;

import com.codingtest.movieticketbookingsystem.common.dto.PageResponse;
import com.codingtest.movieticketbookingsystem.common.enums.ShowStatus;
import com.codingtest.movieticketbookingsystem.common.exception.ResourceNotFoundException;
import com.codingtest.movieticketbookingsystem.common.util.PaginationUtils;
import com.codingtest.movieticketbookingsystem.domain.city.City;
import com.codingtest.movieticketbookingsystem.domain.seat.Seat;
import com.codingtest.movieticketbookingsystem.domain.show.Show;
import com.codingtest.movieticketbookingsystem.domain.show.ShowSeat;
import com.codingtest.movieticketbookingsystem.domain.theater.Theater;
import com.codingtest.movieticketbookingsystem.dto.browse.BrowseCityResponse;
import com.codingtest.movieticketbookingsystem.dto.browse.BrowseSeatResponse;
import com.codingtest.movieticketbookingsystem.dto.browse.BrowseShowResponse;
import com.codingtest.movieticketbookingsystem.dto.browse.BrowseTheaterResponse;
import com.codingtest.movieticketbookingsystem.repository.CityRepository;
import com.codingtest.movieticketbookingsystem.repository.ShowRepository;
import com.codingtest.movieticketbookingsystem.repository.ShowSeatRepository;
import com.codingtest.movieticketbookingsystem.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BrowseService {

    private final CityRepository cityRepository;
    private final TheaterRepository theaterRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final SeatAvailabilityService seatAvailabilityService;

    @Transactional(readOnly = true)
    public PageResponse<BrowseCityResponse> listCities(Integer page, Integer size) {
        Pageable pageable = PaginationUtils.of(page, size, Sort.by("name").ascending());
        return PageResponse.from(cityRepository.findAll(pageable).map(this::toCityResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<BrowseTheaterResponse> listTheatersByCity(Long cityId, Integer page, Integer size) {
        findCity(cityId);
        Pageable pageable = PaginationUtils.of(page, size, Sort.by("name").ascending());
        return PageResponse.from(
                theaterRepository.findByCityIdOrderByNameAsc(cityId, pageable).map(this::toTheaterResponse)
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<BrowseShowResponse> listShows(Long cityId, LocalDate date, Integer page, Integer size) {
        Pageable pageable = PaginationUtils.of(page, size, Sort.by("startTime").ascending());
        Page<Show> shows;

        if (cityId != null && date != null) {
            findCity(cityId);
            Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            shows = showRepository.findScheduledShowsByCityAndStartTimeBetween(
                    cityId, ShowStatus.SCHEDULED, start, end, pageable
            );
        } else if (cityId != null) {
            findCity(cityId);
            Instant now = Instant.now();
            shows = showRepository.findScheduledShowsByCityAndStartTimeBetween(
                    cityId, ShowStatus.SCHEDULED, now, Instant.parse("2099-12-31T23:59:59Z"), pageable
            );
        } else {
            shows = showRepository.findUpcomingShows(ShowStatus.SCHEDULED, Instant.now(), pageable);
        }

        return PageResponse.from(shows.map(this::toShowResponse));
    }

    @Transactional(readOnly = true)
    public BrowseShowResponse getShow(Long showId) {
        return toShowResponse(findShow(showId));
    }

    @Transactional(readOnly = true)
    public PageResponse<BrowseSeatResponse> listSeatsForShow(Long showId, Integer page, Integer size) {
        Show show = findShow(showId);
        if (show.getStatus() != ShowStatus.SCHEDULED) {
            throw new com.codingtest.movieticketbookingsystem.common.exception.BusinessException(
                    "Show is not available for booking",
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "SHOW_NOT_AVAILABLE"
            );
        }

        Pageable pageable = PaginationUtils.of(page, size);
        Page<ShowSeat> allocations = showSeatRepository.findByShow_Id(showId, pageable);
        List<Long> seatIds = allocations.getContent().stream()
                .map(allocation -> allocation.getSeat().getId())
                .toList();
        Map<Long, com.codingtest.movieticketbookingsystem.common.enums.SeatAvailabilityStatus> statuses =
                seatAvailabilityService.getStatusesForShow(showId, seatIds);

        return PageResponse.from(allocations.map(allocation -> {
            Seat seat = allocation.getSeat();
            return BrowseSeatResponse.builder()
                    .id(seat.getId())
                    .rowLabel(seat.getRowLabel())
                    .seatNumber(seat.getSeatNumber())
                    .category(seat.getCategory())
                    .status(statuses.get(seat.getId()))
                    .build();
        }));
    }

    Show findShow(Long showId) {
        return showRepository.findByIdWithDetails(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show", showId));
    }

    private City findCity(Long cityId) {
        return cityRepository.findById(cityId)
                .orElseThrow(() -> new ResourceNotFoundException("City", cityId));
    }

    private BrowseCityResponse toCityResponse(City city) {
        return BrowseCityResponse.builder()
                .id(city.getId())
                .name(city.getName())
                .build();
    }

    private BrowseTheaterResponse toTheaterResponse(Theater theater) {
        return BrowseTheaterResponse.builder()
                .id(theater.getId())
                .cityId(theater.getCity().getId())
                .cityName(theater.getCity().getName())
                .name(theater.getName())
                .address(theater.getAddress())
                .build();
    }

    private BrowseShowResponse toShowResponse(Show show) {
        BigDecimal multiplier = show.getPricingTier() != null
                ? show.getPricingTier().getMultiplier()
                : BigDecimal.ONE;

        return BrowseShowResponse.builder()
                .id(show.getId())
                .theaterId(show.getTheater().getId())
                .theaterName(show.getTheater().getName())
                .cityId(show.getTheater().getCity().getId())
                .cityName(show.getTheater().getCity().getName())
                .movieTitle(show.getMovieTitle())
                .startTime(show.getStartTime())
                .endTime(show.getEndTime())
                .basePrice(show.getBasePrice())
                .pricingTierName(show.getPricingTier() != null ? show.getPricingTier().getName() : null)
                .pricingMultiplier(multiplier)
                .build();
    }
}
