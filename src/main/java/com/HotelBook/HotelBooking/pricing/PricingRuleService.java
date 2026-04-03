package com.HotelBook.HotelBooking.pricing;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class PricingRuleService {

    private final PricingRuleRepository ruleRepository;

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalPrice(UUID roomId, BigDecimal basePrice,
                                          LocalDate checkIn, LocalDate checkOut) {

        // One query for all rules — reused for every night in the loop
        List<PricingRule> activeRules = ruleRepository.findByRoomIdAndIsActiveTrue(roomId);

        BigDecimal total = BigDecimal.ZERO;
        LocalDate current = checkIn;

        while (current.isBefore(checkOut)) {
            BigDecimal nightPrice = calculateNightPrice(basePrice, current, activeRules);
            total = total.add(nightPrice);
            current = current.plusDays(1);
        }

        log.debug("Calculated total price for room {} ({} to {}): {}",
                roomId, checkIn, checkOut, total);

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateNightPrice(BigDecimal basePrice, LocalDate date,
                                          List<PricingRule> rules) {

        Optional<PricingRule> applicableRule = rules.stream()
                .filter(rule -> rule.isApplicableOnDate(date))
                .max(Comparator.comparingInt(PricingRule::getPriority));

        if (applicableRule.isEmpty()) {
            return basePrice; // No rule → base price (no adjustment)
        }

        BigDecimal nightPrice = basePrice.multiply(applicableRule.get().getMultiplier());
        return nightPrice.setScale(2, RoundingMode.HALF_UP);
    }


    @Transactional(readOnly = true)
    public BigDecimal calculatePriceForDate(UUID roomId, BigDecimal basePrice, LocalDate date) {
        List<PricingRule> rules = ruleRepository.findByRoomIdAndIsActiveTrue(roomId);
        return calculateNightPrice(basePrice, date, rules);
    }

    @Transactional(readOnly = true)
    public List<PricingRuleResponseDTO> getRulesForRoom(UUID roomId) {
        return ruleRepository.findByRoomId(roomId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<PricingRuleResponseDTO> getActiveRulesForRoom(UUID roomId) {
        return ruleRepository.findByRoomIdAndIsActiveTrue(roomId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PricingRuleResponseDTO createRule(UUID roomId, PricingRuleRequestDTO request) {
        validateRequest(request);

        PricingRule rule = PricingRule.builder()
                .roomId(roomId)
                .ruleType(request.getRuleType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .dayOfWeek(request.getDayOfWeek())
                .multiplier(request.getMultiplier())
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .description(request.getDescription())
                .build();

        PricingRule saved = ruleRepository.save(rule);
        log.info("Pricing rule created for room {}: {} (multiplier={})",
                roomId, request.getRuleType(), request.getMultiplier());
        return toResponseDTO(saved);
    }


    @Transactional
    public PricingRuleResponseDTO updateRule(UUID ruleId, PricingRuleRequestDTO request) {
        PricingRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException(
                        "Pricing rule not found with id: " + ruleId));

        if (request.getRuleType() != null)  rule.setRuleType(request.getRuleType());
        if (request.getStartDate() != null) rule.setStartDate(request.getStartDate());
        if (request.getEndDate() != null)   rule.setEndDate(request.getEndDate());
        if (request.getDayOfWeek() != null) rule.setDayOfWeek(request.getDayOfWeek());
        if (request.getMultiplier() != null) rule.setMultiplier(request.getMultiplier());
        if (request.getPriority() != null)  rule.setPriority(request.getPriority());
        if (request.getIsActive() != null)  rule.setIsActive(request.getIsActive());
        if (request.getDescription() != null) rule.setDescription(request.getDescription());

        // Re-validate after applying updates
        validateRule(rule);

        PricingRule saved = ruleRepository.save(rule);
        log.info("Pricing rule {} updated", ruleId);
        return toResponseDTO(saved);
    }


    @Transactional
    public PricingRuleResponseDTO toggleRule(UUID ruleId) {
        PricingRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException(
                        "Pricing rule not found with id: " + ruleId));
        rule.setIsActive(!rule.getIsActive());
        return toResponseDTO(ruleRepository.save(rule));
    }


    @Transactional
    public void deleteRule(UUID ruleId) {
        if (!ruleRepository.existsById(ruleId)) {
            throw new RuntimeException("Pricing rule not found with id: " + ruleId);
        }
        ruleRepository.deleteById(ruleId);
        log.info("Pricing rule {} deleted", ruleId);
    }

    // ── VALIDATION ────────────────────────────────────────────────────────────

    /** Validates a request DTO before creating a rule. */
    private void validateRequest(PricingRuleRequestDTO request) {
        switch (request.getRuleType()) {

            case SEASONAL:
            case SPECIAL_EVENT:
                if (request.getStartDate() == null || request.getEndDate() == null) {
                    throw new RuntimeException(
                            request.getRuleType() + " rules require both startDate and endDate.");
                }
                if (request.getStartDate().isAfter(request.getEndDate())) {
                    throw new RuntimeException("startDate must be before or equal to endDate.");
                }
                break;

            case WEEKDAY_WEEKEND:
                if (request.getDayOfWeek() == null || request.getDayOfWeek().isBlank()) {
                    throw new RuntimeException(
                            "WEEKDAY_WEEKEND rules require a dayOfWeek value (e.g. \"FRIDAY,SATURDAY\").");
                }
                break;
        }
    }

    /** Validates an entity before saving (used after partial update). */
    private void validateRule(PricingRule rule) {
        switch (rule.getRuleType()) {
            case SEASONAL:
            case SPECIAL_EVENT:
                if (rule.getStartDate() != null && rule.getEndDate() != null
                        && rule.getStartDate().isAfter(rule.getEndDate())) {
                    throw new RuntimeException("startDate must be before or equal to endDate.");
                }
                break;
            case WEEKDAY_WEEKEND:
                if (rule.getDayOfWeek() == null || rule.getDayOfWeek().isBlank()) {
                    throw new RuntimeException(
                            "WEEKDAY_WEEKEND rules require a dayOfWeek value.");
                }
                break;
        }
    }

    // ── MAPPER ────────────────────────────────────────────────────────────────

    private PricingRuleResponseDTO toResponseDTO(PricingRule rule) {
        PricingRuleResponseDTO dto = new PricingRuleResponseDTO();
        dto.setId(rule.getId());
        dto.setRoomId(rule.getRoomId());
        dto.setRuleType(rule.getRuleType().name()); // enum → String
        dto.setStartDate(rule.getStartDate());
        dto.setEndDate(rule.getEndDate());
        dto.setDayOfWeek(rule.getDayOfWeek());
        dto.setMultiplier(rule.getMultiplier());
        dto.setPriority(rule.getPriority());
        dto.setIsActive(rule.getIsActive());
        dto.setDescription(rule.getDescription());
        return dto;
    }
}
