package com.codingtest.movieticketbookingsystem.service.admin;

import com.codingtest.movieticketbookingsystem.common.dto.PageResponse;
import com.codingtest.movieticketbookingsystem.common.enums.*;
import com.codingtest.movieticketbookingsystem.common.exception.BusinessException;
import com.codingtest.movieticketbookingsystem.common.exception.ResourceNotFoundException;
import com.codingtest.movieticketbookingsystem.common.util.PaginationUtils;
import com.codingtest.movieticketbookingsystem.domain.city.City;
import com.codingtest.movieticketbookingsystem.domain.discount.DiscountCode;
import com.codingtest.movieticketbookingsystem.domain.pricing.PricingTier;
import com.codingtest.movieticketbookingsystem.domain.refund.RefundPolicy;
import com.codingtest.movieticketbookingsystem.domain.seat.Seat;
import com.codingtest.movieticketbookingsystem.domain.show.Show;
import com.codingtest.movieticketbookingsystem.domain.show.ShowSeat;
import com.codingtest.movieticketbookingsystem.domain.theater.Theater;
import com.codingtest.movieticketbookingsystem.dto.admin.city.CityRequest;
import com.codingtest.movieticketbookingsystem.dto.admin.city.CityResponse;
import com.codingtest.movieticketbookingsystem.dto.admin.discount.DiscountCodeRequest;
import com.codingtest.movieticketbookingsystem.dto.admin.discount.DiscountCodeResponse;
import com.codingtest.movieticketbookingsystem.dto.admin.pricing.PricingTierRequest;
import com.codingtest.movieticketbookingsystem.dto.admin.pricing.PricingTierResponse;
import com.codingtest.movieticketbookingsystem.dto.admin.refund.RefundPolicyRequest;
import com.codingtest.movieticketbookingsystem.dto.admin.refund.RefundPolicyResponse;
import com.codingtest.movieticketbookingsystem.dto.admin.seat.SeatLayoutRequest;
import com.codingtest.movieticketbookingsystem.dto.admin.seat.SeatRequest;
import com.codingtest.movieticketbookingsystem.dto.admin.seat.SeatResponse;
import com.codingtest.movieticketbookingsystem.dto.admin.show.ShowRequest;
import com.codingtest.movieticketbookingsystem.dto.admin.show.ShowResponse;
import com.codingtest.movieticketbookingsystem.dto.admin.theater.TheaterRequest;
import com.codingtest.movieticketbookingsystem.dto.admin.theater.TheaterResponse;
import com.codingtest.movieticketbookingsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminCatalogService {

    private final CityRepository cityRepository;
    private final TheaterRepository theaterRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final PricingTierRepository pricingTierRepository;
    private final DiscountCodeRepository discountCodeRepository;
    private final RefundPolicyRepository refundPolicyRepository;
    private final BookingRepository bookingRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final SeatHoldItemRepository seatHoldItemRepository;
    private final BookingSeatRepository bookingSeatRepository;

    // --- Cities ---

    @Transactional
    public CityResponse createCity(CityRequest request) {
        String name = request.getName().trim();
        if (cityRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("City already exists", HttpStatus.CONFLICT, "CITY_ALREADY_EXISTS");
        }
        return toCityResponse(cityRepository.save(City.builder().name(name).build()));
    }

    @Transactional(readOnly = true)
    public PageResponse<CityResponse> listCities(Integer page, Integer size) {
        Pageable pageable = PaginationUtils.of(page, size, Sort.by("name").ascending());
        return PageResponse.from(cityRepository.findAll(pageable).map(this::toCityResponse));
    }

    @Transactional(readOnly = true)
    public CityResponse getCity(Long id) {
        return toCityResponse(findCity(id));
    }

    @Transactional
    public CityResponse updateCity(Long id, CityRequest request) {
        City city = findCity(id);
        String name = request.getName().trim();
        cityRepository.findByNameIgnoreCase(name)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException("City already exists", HttpStatus.CONFLICT, "CITY_ALREADY_EXISTS");
                });
        city.setName(name);
        return toCityResponse(cityRepository.save(city));
    }

    @Transactional
    public void deleteCity(Long id) {
        City city = findCity(id);
        if (theaterRepository.existsByCityId(city.getId())) {
            throw new BusinessException("Cannot delete city with existing theaters", HttpStatus.CONFLICT, "CITY_HAS_THEATERS");
        }
        cityRepository.delete(city);
    }

    // --- Theaters ---

    @Transactional
    public TheaterResponse createTheater(TheaterRequest request) {
        City city = findCity(request.getCityId());
        String name = request.getName().trim();
        if (theaterRepository.existsByCityIdAndNameIgnoreCase(city.getId(), name)) {
            throw new BusinessException("Theater already exists in this city", HttpStatus.CONFLICT, "THEATER_ALREADY_EXISTS");
        }
        Theater theater = Theater.builder()
                .city(city)
                .name(name)
                .address(trimToNull(request.getAddress()))
                .build();
        return toTheaterResponse(theaterRepository.save(theater));
    }

    @Transactional(readOnly = true)
    public PageResponse<TheaterResponse> listTheaters(Long cityId, Integer page, Integer size) {
        Pageable pageable = PaginationUtils.of(page, size, Sort.by("name").ascending());
        Page<Theater> theaters = cityId == null
                ? theaterRepository.findAll(pageable)
                : theaterRepository.findByCityIdOrderByNameAsc(cityId, pageable);
        return PageResponse.from(theaters.map(this::toTheaterResponse));
    }

    @Transactional(readOnly = true)
    public TheaterResponse getTheater(Long id) {
        return toTheaterResponse(findTheater(id));
    }

    @Transactional
    public TheaterResponse updateTheater(Long id, TheaterRequest request) {
        Theater theater = findTheater(id);
        City city = findCity(request.getCityId());
        String name = request.getName().trim();
        theaterRepository.findByCityIdOrderByNameAsc(city.getId()).stream()
                .filter(existing -> existing.getName().equalsIgnoreCase(name))
                .filter(existing -> !existing.getId().equals(id))
                .findFirst()
                .ifPresent(existing -> {
                    throw new BusinessException("Theater already exists in this city", HttpStatus.CONFLICT, "THEATER_ALREADY_EXISTS");
                });
        theater.setCity(city);
        theater.setName(name);
        theater.setAddress(trimToNull(request.getAddress()));
        return toTheaterResponse(theaterRepository.save(theater));
    }

    @Transactional
    public void deleteTheater(Long id) {
        Theater theater = findTheater(id);
        if (showRepository.existsByTheaterId(theater.getId()) || seatRepository.existsByTheaterId(theater.getId())) {
            throw new BusinessException("Cannot delete theater with existing shows or seats", HttpStatus.CONFLICT, "THEATER_HAS_DEPENDENCIES");
        }
        theaterRepository.delete(theater);
    }

    // --- Seats ---

    @Transactional
    public SeatResponse createSeat(Long theaterId, SeatRequest request) {
        Theater theater = findTheater(theaterId);
        Seat seat = buildSeat(theater, request);
        validateUniqueSeatPosition(theater.getId(), seat.getRowLabel(), seat.getSeatNumber(), null);
        return toSeatResponse(seatRepository.save(seat));
    }

    @Transactional
    public List<SeatResponse> createSeatLayout(Long theaterId, SeatLayoutRequest request) {
        Theater theater = findTheater(theaterId);
        Set<String> positions = new HashSet<>();
        List<Seat> seats = request.getSeats().stream()
                .map(seatRequest -> {
                    Seat seat = buildSeat(theater, seatRequest);
                    String key = seat.getRowLabel().toUpperCase() + "-" + seat.getSeatNumber();
                    if (!positions.add(key)) {
                        throw new BusinessException(
                                "Duplicate seat in layout: row " + seat.getRowLabel() + ", number " + seat.getSeatNumber(),
                                HttpStatus.BAD_REQUEST,
                                "DUPLICATE_SEAT_IN_LAYOUT"
                        );
                    }
                    validateUniqueSeatPosition(theater.getId(), seat.getRowLabel(), seat.getSeatNumber(), null);
                    return seat;
                })
                .toList();
        return seatRepository.saveAll(seats).stream().map(this::toSeatResponse).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<SeatResponse> listSeatsByTheater(Long theaterId, Integer page, Integer size) {
        findTheater(theaterId);
        Pageable pageable = PaginationUtils.of(page, size, Sort.by("rowLabel").ascending().and(Sort.by("seatNumber").ascending()));
        return PageResponse.from(
                seatRepository.findByTheaterIdOrderByRowLabelAscSeatNumberAsc(theaterId, pageable).map(this::toSeatResponse)
        );
    }

    @Transactional(readOnly = true)
    public SeatResponse getSeat(Long id) {
        return toSeatResponse(findSeat(id));
    }

    @Transactional
    public SeatResponse updateSeat(Long id, SeatRequest request) {
        Seat seat = findSeat(id);
        String rowLabel = normalizeRowLabel(request.getRowLabel());
        validateUniqueSeatPosition(seat.getTheater().getId(), rowLabel, request.getSeatNumber(), seat.getId());
        seat.setRowLabel(rowLabel);
        seat.setSeatNumber(request.getSeatNumber());
        seat.setCategory(request.getCategory() != null ? request.getCategory() : SeatCategory.REGULAR);
        return toSeatResponse(seatRepository.save(seat));
    }

    @Transactional
    public void deleteSeat(Long id) {
        Seat seat = findSeat(id);
        Instant now = Instant.now();
        if (seatHoldItemRepository.existsActiveHoldForSeatId(id, HoldStatus.ACTIVE, now)) {
            throw new BusinessException("Cannot delete seat with an active hold", HttpStatus.CONFLICT, "SEAT_HAS_ACTIVE_HOLD");
        }
        if (bookingSeatRepository.existsBySeat_IdAndBooking_Status(seat.getId(), BookingStatus.CONFIRMED)) {
            throw new BusinessException("Cannot delete seat with a confirmed booking", HttpStatus.CONFLICT, "SEAT_HAS_CONFIRMED_BOOKING");
        }
        seatRepository.delete(seat);
    }

    // --- Shows (auto-allocates theater seats to show) ---

    @Transactional
    public ShowResponse createShow(ShowRequest request) {
        validateShowTimes(request);
        Theater theater = findTheater(request.getTheaterId());
        PricingTier pricingTier = resolvePricingTier(request.getPricingTierId());

        Show show = Show.builder()
                .theater(theater)
                .movieTitle(request.getMovieTitle().trim())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .basePrice(request.getBasePrice())
                .pricingTier(pricingTier)
                .status(request.getStatus() != null ? request.getStatus() : ShowStatus.SCHEDULED)
                .build();

        show = showRepository.save(show);
        allocateTheaterSeatsToShow(show, theater);
        return toShowResponse(show);
    }

    @Transactional(readOnly = true)
    public PageResponse<ShowResponse> listShows(Long theaterId, Integer page, Integer size) {
        Pageable pageable = PaginationUtils.of(page, size, Sort.by("startTime").ascending());
        Page<Show> shows = theaterId == null
                ? showRepository.findAll(pageable)
                : showRepository.findByTheaterIdOrderByStartTimeAsc(theaterId, pageable);
        return PageResponse.from(shows.map(this::toShowResponse));
    }

    @Transactional(readOnly = true)
    public ShowResponse getShow(Long id) {
        return toShowResponse(findShow(id));
    }

    @Transactional
    public ShowResponse updateShow(Long id, ShowRequest request) {
        validateShowTimes(request);
        Show show = findShow(id);
        Theater theater = findTheater(request.getTheaterId());
        show.setTheater(theater);
        show.setMovieTitle(request.getMovieTitle().trim());
        show.setStartTime(request.getStartTime());
        show.setEndTime(request.getEndTime());
        show.setBasePrice(request.getBasePrice());
        show.setPricingTier(resolvePricingTier(request.getPricingTierId()));
        if (request.getStatus() != null) {
            show.setStatus(request.getStatus());
        }
        return toShowResponse(showRepository.save(show));
    }

    @Transactional
    public void deleteShow(Long id) {
        Show show = findShow(id);
        if (bookingRepository.existsByShow_Id(id)) {
            throw new BusinessException("Cannot delete show with existing bookings", HttpStatus.CONFLICT, "SHOW_HAS_BOOKINGS");
        }
        if (seatHoldRepository.existsByShow_Id(id)) {
            throw new BusinessException("Cannot delete show with existing seat holds", HttpStatus.CONFLICT, "SHOW_HAS_HOLDS");
        }
        showRepository.delete(show);
    }

    // --- Pricing tiers ---

    @Transactional
    public PricingTierResponse createPricingTier(PricingTierRequest request) {
        String name = request.getName().trim();
        if (pricingTierRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("Pricing tier already exists", HttpStatus.CONFLICT, "PRICING_TIER_ALREADY_EXISTS");
        }
        PricingTier tier = PricingTier.builder()
                .name(name)
                .tierType(request.getTierType())
                .multiplier(request.getMultiplier())
                .description(trimToNull(request.getDescription()))
                .active(request.getActive() == null || request.getActive())
                .build();
        return toPricingTierResponse(pricingTierRepository.save(tier));
    }

    @Transactional(readOnly = true)
    public PageResponse<PricingTierResponse> listPricingTiers(Integer page, Integer size) {
        Pageable pageable = PaginationUtils.of(page, size, Sort.by("name").ascending());
        return PageResponse.from(pricingTierRepository.findAll(pageable).map(this::toPricingTierResponse));
    }

    @Transactional(readOnly = true)
    public PricingTierResponse getPricingTier(Long id) {
        return toPricingTierResponse(findPricingTier(id));
    }

    @Transactional
    public PricingTierResponse updatePricingTier(Long id, PricingTierRequest request) {
        PricingTier tier = findPricingTier(id);
        String name = request.getName().trim();
        pricingTierRepository.findByNameIgnoreCase(name)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException("Pricing tier already exists", HttpStatus.CONFLICT, "PRICING_TIER_ALREADY_EXISTS");
                });
        tier.setName(name);
        tier.setTierType(request.getTierType());
        tier.setMultiplier(request.getMultiplier());
        tier.setDescription(trimToNull(request.getDescription()));
        if (request.getActive() != null) {
            tier.setActive(request.getActive());
        }
        return toPricingTierResponse(pricingTierRepository.save(tier));
    }

    @Transactional
    public void deletePricingTier(Long id) {
        PricingTier tier = findPricingTier(id);
        if (showRepository.existsByPricingTier_Id(id)) {
            throw new BusinessException("Cannot delete pricing tier assigned to shows", HttpStatus.CONFLICT, "PRICING_TIER_IN_USE");
        }
        pricingTierRepository.delete(tier);
    }

    // --- Refund policies ---

    @Transactional
    public RefundPolicyResponse createRefundPolicy(RefundPolicyRequest request) {
        RefundPolicy policy = RefundPolicy.builder()
                .name(request.getName().trim())
                .hoursBeforeShow(request.getHoursBeforeShow())
                .refundPercentage(request.getRefundPercentage())
                .active(request.getActive() == null || request.getActive())
                .build();
        return toRefundPolicyResponse(refundPolicyRepository.save(policy));
    }

    @Transactional(readOnly = true)
    public PageResponse<RefundPolicyResponse> listRefundPolicies(Integer page, Integer size) {
        Pageable pageable = PaginationUtils.of(page, size, Sort.by("hoursBeforeShow").descending());
        return PageResponse.from(refundPolicyRepository.findAll(pageable).map(this::toRefundPolicyResponse));
    }

    @Transactional(readOnly = true)
    public RefundPolicyResponse getRefundPolicy(Long id) {
        return toRefundPolicyResponse(findRefundPolicy(id));
    }

    @Transactional
    public RefundPolicyResponse updateRefundPolicy(Long id, RefundPolicyRequest request) {
        RefundPolicy policy = findRefundPolicy(id);
        policy.setName(request.getName().trim());
        policy.setHoursBeforeShow(request.getHoursBeforeShow());
        policy.setRefundPercentage(request.getRefundPercentage());
        if (request.getActive() != null) {
            policy.setActive(request.getActive());
        }
        return toRefundPolicyResponse(refundPolicyRepository.save(policy));
    }

    @Transactional
    public void deleteRefundPolicy(Long id) {
        refundPolicyRepository.delete(findRefundPolicy(id));
    }

    // --- Discount codes ---

    @Transactional
    public DiscountCodeResponse createDiscountCode(DiscountCodeRequest request) {
        validateDiscountRequest(request);
        String code = request.getCode().trim().toUpperCase();
        if (discountCodeRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("Discount code already exists", HttpStatus.CONFLICT, "DISCOUNT_CODE_ALREADY_EXISTS");
        }
        DiscountCode discountCode = DiscountCode.builder()
                .code(code)
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .maxUses(request.getMaxUses())
                .usedCount(0)
                .active(request.getActive() == null || request.getActive())
                .build();
        return toDiscountCodeResponse(discountCodeRepository.save(discountCode));
    }

    @Transactional(readOnly = true)
    public PageResponse<DiscountCodeResponse> listDiscountCodes(Integer page, Integer size) {
        Pageable pageable = PaginationUtils.of(page, size, Sort.by("code").ascending());
        return PageResponse.from(discountCodeRepository.findAll(pageable).map(this::toDiscountCodeResponse));
    }

    @Transactional(readOnly = true)
    public DiscountCodeResponse getDiscountCode(Long id) {
        return toDiscountCodeResponse(findDiscountCode(id));
    }

    @Transactional
    public DiscountCodeResponse updateDiscountCode(Long id, DiscountCodeRequest request) {
        validateDiscountRequest(request);
        DiscountCode discountCode = findDiscountCode(id);
        String code = request.getCode().trim().toUpperCase();
        discountCodeRepository.findByCodeIgnoreCase(code)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException("Discount code already exists", HttpStatus.CONFLICT, "DISCOUNT_CODE_ALREADY_EXISTS");
                });
        discountCode.setCode(code);
        discountCode.setDiscountType(request.getDiscountType());
        discountCode.setDiscountValue(request.getDiscountValue());
        discountCode.setValidFrom(request.getValidFrom());
        discountCode.setValidUntil(request.getValidUntil());
        discountCode.setMaxUses(request.getMaxUses());
        if (request.getActive() != null) {
            discountCode.setActive(request.getActive());
        }
        return toDiscountCodeResponse(discountCodeRepository.save(discountCode));
    }

    @Transactional
    public void deleteDiscountCode(Long id) {
        discountCodeRepository.delete(findDiscountCode(id));
    }

    // --- Helpers ---

    private void allocateTheaterSeatsToShow(Show show, Theater theater) {
        List<Seat> seats = seatRepository.findByTheaterIdOrderByRowLabelAscSeatNumberAsc(theater.getId());
        for (Seat seat : seats) {
            showSeatRepository.save(ShowSeat.builder().show(show).seat(seat).build());
        }
    }

    private City findCity(Long id) {
        return cityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("City", id));
    }

    private Theater findTheater(Long id) {
        return theaterRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Theater", id));
    }

    private Seat findSeat(Long id) {
        return seatRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Seat", id));
    }

    private Show findShow(Long id) {
        return showRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Show", id));
    }

    private PricingTier findPricingTier(Long id) {
        return pricingTierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Pricing tier", id));
    }

    private RefundPolicy findRefundPolicy(Long id) {
        return refundPolicyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Refund policy", id));
    }

    private DiscountCode findDiscountCode(Long id) {
        return discountCodeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Discount code", id));
    }

    private PricingTier resolvePricingTier(Long pricingTierId) {
        if (pricingTierId == null) {
            return null;
        }
        return findPricingTier(pricingTierId);
    }

    private void validateShowTimes(ShowRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException("End time must be after start time", HttpStatus.BAD_REQUEST, "INVALID_SHOW_TIME");
        }
    }

    private void validateDiscountRequest(DiscountCodeRequest request) {
        if (!request.getValidUntil().isAfter(request.getValidFrom())) {
            throw new BusinessException("Valid until must be after valid from", HttpStatus.BAD_REQUEST, "INVALID_DISCOUNT_PERIOD");
        }
        if (request.getDiscountType() == DiscountType.PERCENTAGE
                && request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException("Percentage discount cannot exceed 100", HttpStatus.BAD_REQUEST, "INVALID_DISCOUNT_VALUE");
        }
    }

    private Seat buildSeat(Theater theater, SeatRequest request) {
        return Seat.builder()
                .theater(theater)
                .rowLabel(normalizeRowLabel(request.getRowLabel()))
                .seatNumber(request.getSeatNumber())
                .category(request.getCategory() != null ? request.getCategory() : SeatCategory.REGULAR)
                .build();
    }

    private void validateUniqueSeatPosition(Long theaterId, String rowLabel, Integer seatNumber, Long excludeSeatId) {
        if (!seatRepository.existsByTheaterIdAndRowLabelIgnoreCaseAndSeatNumber(theaterId, rowLabel, seatNumber)) {
            return;
        }
        if (excludeSeatId != null) {
            Seat existing = seatRepository.findByTheaterIdOrderByRowLabelAscSeatNumberAsc(theaterId).stream()
                    .filter(seat -> seat.getRowLabel().equalsIgnoreCase(rowLabel))
                    .filter(seat -> seat.getSeatNumber().equals(seatNumber))
                    .findFirst()
                    .orElse(null);
            if (existing != null && existing.getId().equals(excludeSeatId)) {
                return;
            }
        }
        throw new BusinessException(
                "Seat already exists at row " + rowLabel + ", number " + seatNumber,
                HttpStatus.CONFLICT,
                "SEAT_ALREADY_EXISTS"
        );
    }

    private String normalizeRowLabel(String rowLabel) {
        return rowLabel.trim().toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private CityResponse toCityResponse(City city) {
        return CityResponse.builder()
                .id(city.getId())
                .name(city.getName())
                .createdAt(city.getCreatedAt())
                .updatedAt(city.getUpdatedAt())
                .build();
    }

    private TheaterResponse toTheaterResponse(Theater theater) {
        return TheaterResponse.builder()
                .id(theater.getId())
                .cityId(theater.getCity().getId())
                .cityName(theater.getCity().getName())
                .name(theater.getName())
                .address(theater.getAddress())
                .createdAt(theater.getCreatedAt())
                .updatedAt(theater.getUpdatedAt())
                .build();
    }

    private SeatResponse toSeatResponse(Seat seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .theaterId(seat.getTheater().getId())
                .rowLabel(seat.getRowLabel())
                .seatNumber(seat.getSeatNumber())
                .category(seat.getCategory())
                .createdAt(seat.getCreatedAt())
                .updatedAt(seat.getUpdatedAt())
                .build();
    }

    private ShowResponse toShowResponse(Show show) {
        return ShowResponse.builder()
                .id(show.getId())
                .theaterId(show.getTheater().getId())
                .theaterName(show.getTheater().getName())
                .movieTitle(show.getMovieTitle())
                .startTime(show.getStartTime())
                .endTime(show.getEndTime())
                .basePrice(show.getBasePrice())
                .pricingTierId(show.getPricingTier() != null ? show.getPricingTier().getId() : null)
                .pricingTierName(show.getPricingTier() != null ? show.getPricingTier().getName() : null)
                .status(show.getStatus())
                .createdAt(show.getCreatedAt())
                .updatedAt(show.getUpdatedAt())
                .build();
    }

    private PricingTierResponse toPricingTierResponse(PricingTier tier) {
        return PricingTierResponse.builder()
                .id(tier.getId())
                .name(tier.getName())
                .tierType(tier.getTierType())
                .multiplier(tier.getMultiplier())
                .description(tier.getDescription())
                .active(tier.isActive())
                .createdAt(tier.getCreatedAt())
                .updatedAt(tier.getUpdatedAt())
                .build();
    }

    private RefundPolicyResponse toRefundPolicyResponse(RefundPolicy policy) {
        return RefundPolicyResponse.builder()
                .id(policy.getId())
                .name(policy.getName())
                .hoursBeforeShow(policy.getHoursBeforeShow())
                .refundPercentage(policy.getRefundPercentage())
                .active(policy.isActive())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }

    private DiscountCodeResponse toDiscountCodeResponse(DiscountCode discountCode) {
        return DiscountCodeResponse.builder()
                .id(discountCode.getId())
                .code(discountCode.getCode())
                .discountType(discountCode.getDiscountType())
                .discountValue(discountCode.getDiscountValue())
                .validFrom(discountCode.getValidFrom())
                .validUntil(discountCode.getValidUntil())
                .maxUses(discountCode.getMaxUses())
                .usedCount(discountCode.getUsedCount())
                .active(discountCode.isActive())
                .createdAt(discountCode.getCreatedAt())
                .updatedAt(discountCode.getUpdatedAt())
                .build();
    }
}
