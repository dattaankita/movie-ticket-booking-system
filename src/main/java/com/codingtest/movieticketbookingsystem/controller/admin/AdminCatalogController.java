package com.codingtest.movieticketbookingsystem.controller.admin;

import com.codingtest.movieticketbookingsystem.common.dto.ApiResponse;
import com.codingtest.movieticketbookingsystem.common.dto.PageResponse;
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
import com.codingtest.movieticketbookingsystem.service.admin.AdminCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCatalogController {

    private final AdminCatalogService adminCatalogService;

    @PostMapping("/cities")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CityResponse> createCity(@Valid @RequestBody CityRequest request) {
        return ApiResponse.ok("City created", adminCatalogService.createCity(request));
    }

    @GetMapping("/cities")
    public ApiResponse<PageResponse<CityResponse>> listCities(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.ok(adminCatalogService.listCities(page, size));
    }

    @GetMapping("/cities/{id}")
    public ApiResponse<CityResponse> getCity(@PathVariable Long id) {
        return ApiResponse.ok(adminCatalogService.getCity(id));
    }

    @PutMapping("/cities/{id}")
    public ApiResponse<CityResponse> updateCity(@PathVariable Long id, @Valid @RequestBody CityRequest request) {
        return ApiResponse.ok("City updated", adminCatalogService.updateCity(id, request));
    }

    @DeleteMapping("/cities/{id}")
    public ApiResponse<Void> deleteCity(@PathVariable Long id) {
        adminCatalogService.deleteCity(id);
        return ApiResponse.okMessage("City deleted");
    }

    @PostMapping("/theaters")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TheaterResponse> createTheater(@Valid @RequestBody TheaterRequest request) {
        return ApiResponse.ok("Theater created", adminCatalogService.createTheater(request));
    }

    @GetMapping("/theaters")
    public ApiResponse<PageResponse<TheaterResponse>> listTheaters(
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.ok(adminCatalogService.listTheaters(cityId, page, size));
    }

    @GetMapping("/theaters/{id}")
    public ApiResponse<TheaterResponse> getTheater(@PathVariable Long id) {
        return ApiResponse.ok(adminCatalogService.getTheater(id));
    }

    @PutMapping("/theaters/{id}")
    public ApiResponse<TheaterResponse> updateTheater(@PathVariable Long id, @Valid @RequestBody TheaterRequest request) {
        return ApiResponse.ok("Theater updated", adminCatalogService.updateTheater(id, request));
    }

    @DeleteMapping("/theaters/{id}")
    public ApiResponse<Void> deleteTheater(@PathVariable Long id) {
        adminCatalogService.deleteTheater(id);
        return ApiResponse.okMessage("Theater deleted");
    }

    @PostMapping("/theaters/{theaterId}/seats")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SeatResponse> createSeat(
            @PathVariable Long theaterId,
            @Valid @RequestBody SeatRequest request) {
        return ApiResponse.ok("Seat created", adminCatalogService.createSeat(theaterId, request));
    }

    @PostMapping("/theaters/{theaterId}/seats/layout")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<List<SeatResponse>> createSeatLayout(
            @PathVariable Long theaterId,
            @Valid @RequestBody SeatLayoutRequest request) {
        return ApiResponse.ok("Seat layout created", adminCatalogService.createSeatLayout(theaterId, request));
    }

    @GetMapping("/theaters/{theaterId}/seats")
    public ApiResponse<PageResponse<SeatResponse>> listSeatsByTheater(
            @PathVariable Long theaterId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.ok(adminCatalogService.listSeatsByTheater(theaterId, page, size));
    }

    @GetMapping("/seats/{id}")
    public ApiResponse<SeatResponse> getSeat(@PathVariable Long id) {
        return ApiResponse.ok(adminCatalogService.getSeat(id));
    }

    @PutMapping("/seats/{id}")
    public ApiResponse<SeatResponse> updateSeat(@PathVariable Long id, @Valid @RequestBody SeatRequest request) {
        return ApiResponse.ok("Seat updated", adminCatalogService.updateSeat(id, request));
    }

    @DeleteMapping("/seats/{id}")
    public ApiResponse<Void> deleteSeat(@PathVariable Long id) {
        adminCatalogService.deleteSeat(id);
        return ApiResponse.okMessage("Seat deleted");
    }

    @PostMapping("/shows")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ShowResponse> createShow(@Valid @RequestBody ShowRequest request) {
        return ApiResponse.ok("Show created", adminCatalogService.createShow(request));
    }

    @GetMapping("/shows")
    public ApiResponse<PageResponse<ShowResponse>> listShows(
            @RequestParam(required = false) Long theaterId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.ok(adminCatalogService.listShows(theaterId, page, size));
    }

    @GetMapping("/shows/{id}")
    public ApiResponse<ShowResponse> getShow(@PathVariable Long id) {
        return ApiResponse.ok(adminCatalogService.getShow(id));
    }

    @PutMapping("/shows/{id}")
    public ApiResponse<ShowResponse> updateShow(@PathVariable Long id, @Valid @RequestBody ShowRequest request) {
        return ApiResponse.ok("Show updated", adminCatalogService.updateShow(id, request));
    }

    @DeleteMapping("/shows/{id}")
    public ApiResponse<Void> deleteShow(@PathVariable Long id) {
        adminCatalogService.deleteShow(id);
        return ApiResponse.okMessage("Show deleted");
    }

    @PostMapping("/pricing-tiers")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PricingTierResponse> createPricingTier(@Valid @RequestBody PricingTierRequest request) {
        return ApiResponse.ok("Pricing tier created", adminCatalogService.createPricingTier(request));
    }

    @GetMapping("/pricing-tiers")
    public ApiResponse<PageResponse<PricingTierResponse>> listPricingTiers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.ok(adminCatalogService.listPricingTiers(page, size));
    }

    @GetMapping("/pricing-tiers/{id}")
    public ApiResponse<PricingTierResponse> getPricingTier(@PathVariable Long id) {
        return ApiResponse.ok(adminCatalogService.getPricingTier(id));
    }

    @PutMapping("/pricing-tiers/{id}")
    public ApiResponse<PricingTierResponse> updatePricingTier(
            @PathVariable Long id,
            @Valid @RequestBody PricingTierRequest request) {
        return ApiResponse.ok("Pricing tier updated", adminCatalogService.updatePricingTier(id, request));
    }

    @DeleteMapping("/pricing-tiers/{id}")
    public ApiResponse<Void> deletePricingTier(@PathVariable Long id) {
        adminCatalogService.deletePricingTier(id);
        return ApiResponse.okMessage("Pricing tier deleted");
    }

    @PostMapping("/refund-policies")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RefundPolicyResponse> createRefundPolicy(@Valid @RequestBody RefundPolicyRequest request) {
        return ApiResponse.ok("Refund policy created", adminCatalogService.createRefundPolicy(request));
    }

    @GetMapping("/refund-policies")
    public ApiResponse<PageResponse<RefundPolicyResponse>> listRefundPolicies(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.ok(adminCatalogService.listRefundPolicies(page, size));
    }

    @GetMapping("/refund-policies/{id}")
    public ApiResponse<RefundPolicyResponse> getRefundPolicy(@PathVariable Long id) {
        return ApiResponse.ok(adminCatalogService.getRefundPolicy(id));
    }

    @PutMapping("/refund-policies/{id}")
    public ApiResponse<RefundPolicyResponse> updateRefundPolicy(
            @PathVariable Long id,
            @Valid @RequestBody RefundPolicyRequest request) {
        return ApiResponse.ok("Refund policy updated", adminCatalogService.updateRefundPolicy(id, request));
    }

    @DeleteMapping("/refund-policies/{id}")
    public ApiResponse<Void> deleteRefundPolicy(@PathVariable Long id) {
        adminCatalogService.deleteRefundPolicy(id);
        return ApiResponse.okMessage("Refund policy deleted");
    }

    @PostMapping("/discount-codes")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DiscountCodeResponse> createDiscountCode(@Valid @RequestBody DiscountCodeRequest request) {
        return ApiResponse.ok("Discount code created", adminCatalogService.createDiscountCode(request));
    }

    @GetMapping("/discount-codes")
    public ApiResponse<PageResponse<DiscountCodeResponse>> listDiscountCodes(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.ok(adminCatalogService.listDiscountCodes(page, size));
    }

    @GetMapping("/discount-codes/{id}")
    public ApiResponse<DiscountCodeResponse> getDiscountCode(@PathVariable Long id) {
        return ApiResponse.ok(adminCatalogService.getDiscountCode(id));
    }

    @PutMapping("/discount-codes/{id}")
    public ApiResponse<DiscountCodeResponse> updateDiscountCode(
            @PathVariable Long id,
            @Valid @RequestBody DiscountCodeRequest request) {
        return ApiResponse.ok("Discount code updated", adminCatalogService.updateDiscountCode(id, request));
    }

    @DeleteMapping("/discount-codes/{id}")
    public ApiResponse<Void> deleteDiscountCode(@PathVariable Long id) {
        adminCatalogService.deleteDiscountCode(id);
        return ApiResponse.okMessage("Discount code deleted");
    }
}
