package com.HotelBook.HotelBooking.pricing;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
public class PricingRuleController {

    private final PricingRuleService pricingService;


    @GetMapping("/api/hotels/{hotelId}/rooms/{roomId}/pricing-rules")
    public ResponseEntity<List<PricingRuleResponseDTO>> getRules(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId) {
        return ResponseEntity.ok(pricingService.getRulesForRoom(roomId));
    }


    @PostMapping("/api/hotels/{hotelId}/rooms/{roomId}/pricing-rules")
    public ResponseEntity<PricingRuleResponseDTO> createRule(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            @Valid @RequestBody PricingRuleRequestDTO request) {

        PricingRuleResponseDTO created = pricingService.createRule(roomId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @PutMapping("/api/hotels/{hotelId}/rooms/{roomId}/pricing-rules/{ruleId}")
    public ResponseEntity<PricingRuleResponseDTO> updateRule(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            @PathVariable UUID ruleId,
            @Valid @RequestBody PricingRuleRequestDTO request) {

        return ResponseEntity.ok(pricingService.updateRule(ruleId, request));
    }


    @PatchMapping("/api/hotels/{hotelId}/rooms/{roomId}/pricing-rules/{ruleId}/toggle")
    public ResponseEntity<PricingRuleResponseDTO> toggleRule(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            @PathVariable UUID ruleId) {

        return ResponseEntity.ok(pricingService.toggleRule(ruleId));
    }

    @DeleteMapping("/api/hotels/{hotelId}/rooms/{roomId}/pricing-rules/{ruleId}")
    public ResponseEntity<Void> deleteRule(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            @PathVariable UUID ruleId) {

        pricingService.deleteRule(ruleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/rooms/{roomId}/price")
    public ResponseEntity<PricePreviewResponse> getPriceForDate(
            @PathVariable UUID roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam BigDecimal basePrice) {

        BigDecimal finalPrice = pricingService.calculatePriceForDate(roomId, basePrice, date);
        BigDecimal multiplier = basePrice.compareTo(BigDecimal.ZERO) > 0
                ? finalPrice.divide(basePrice, 4, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ONE;

        PricePreviewResponse preview = new PricePreviewResponse(date, basePrice, finalPrice, multiplier);
        return ResponseEntity.ok(preview);
    }

    /**
     * GET /api/rooms/{roomId}/pricing-rules/active
     *
     * Returns only ACTIVE rules — used by the customer-facing room detail page
     * to show pricing information (e.g. "Weekend rates apply").
     */
    @GetMapping("/api/rooms/{roomId}/pricing-rules/active")
    public ResponseEntity<List<PricingRuleResponseDTO>> getActiveRules(
            @PathVariable UUID roomId) {
        return ResponseEntity.ok(pricingService.getActiveRulesForRoom(roomId));
    }

    // ── INNER RESPONSE CLASS ──────────────────────────────────────────────────

    /** Response for the price-preview endpoint. */
    public static class PricePreviewResponse {
        private final LocalDate date;
        private final BigDecimal basePrice;
        private final BigDecimal finalPrice;
        private final BigDecimal effectiveMultiplier;

        public PricePreviewResponse(LocalDate date, BigDecimal basePrice,
                                    BigDecimal finalPrice, BigDecimal effectiveMultiplier) {
            this.date = date;
            this.basePrice = basePrice;
            this.finalPrice = finalPrice;
            this.effectiveMultiplier = effectiveMultiplier;
        }

        public LocalDate getDate() { return date; }
        public BigDecimal getBasePrice() { return basePrice; }
        public BigDecimal getFinalPrice() { return finalPrice; }
        public BigDecimal getEffectiveMultiplier() { return effectiveMultiplier; }
    }
}
