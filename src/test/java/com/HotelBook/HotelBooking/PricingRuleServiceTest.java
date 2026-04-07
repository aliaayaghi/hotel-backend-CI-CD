package com.HotelBook.HotelBooking;

import com.HotelBook.HotelBooking.Pricing.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingRuleServiceTest {

    @Mock
    private PricingRuleRepository ruleRepository;

    @InjectMocks
    private PricingRuleService pricingRuleService;

    private UUID roomId;
    private BigDecimal basePrice;

    @BeforeEach
    void setUp() {
        roomId = UUID.randomUUID();
        basePrice = new BigDecimal("100.00");
    }

    @Test
    @DisplayName("calculateNightPrice: Should use the rule with the highest priority")
    void calculateNightPrice_PriorityLogic() {
        LocalDate testDate = LocalDate.of(2024, 12, 25);

        // Rule 1: Seasonal (Lower Priority)
        PricingRule seasonalRule = mock(PricingRule.class);
        when(seasonalRule.isApplicableOnDate(testDate)).thenReturn(true);
        when(seasonalRule.getPriority()).thenReturn(1);
        // multiplier might not be called if this rule loses the priority contest
        lenient().when(seasonalRule.getMultiplier()).thenReturn(new BigDecimal("1.5"));

        // Rule 2: Special Event (Higher Priority)
        PricingRule eventRule = mock(PricingRule.class);
        when(eventRule.isApplicableOnDate(testDate)).thenReturn(true);
        when(eventRule.getPriority()).thenReturn(10);
        when(eventRule.getMultiplier()).thenReturn(new BigDecimal("2.0"));

        List<PricingRule> rules = Arrays.asList(seasonalRule, eventRule);

        // Act
        BigDecimal result = pricingRuleService.calculateNightPrice(basePrice, testDate, rules);

        // Assert: Using compareTo for BigDecimal to ignore scale differences
        assertTrue(new BigDecimal("200.00").compareTo(result) == 0);
    }

    @Test
    @DisplayName("calculateTotalPrice: Should iterate correctly through multiple nights")
    void calculateTotalPrice_MultipleNights() {
        // Arrange: 2-night stay (Oct 1 to Oct 3)
        LocalDate checkIn = LocalDate.of(2024, 10, 1);
        LocalDate checkOut = LocalDate.of(2024, 10, 3);

        PricingRule rule = mock(PricingRule.class);
        // We use lenient() here because the loop calls this for multiple dates,
        // and strict stubbing can be picky about exact invocation counts/orders
        lenient().when(rule.isApplicableOnDate(checkIn)).thenReturn(true);
        lenient().when(rule.isApplicableOnDate(checkIn.plusDays(1))).thenReturn(false);
        lenient().when(rule.getPriority()).thenReturn(1);
        lenient().when(rule.getMultiplier()).thenReturn(new BigDecimal("1.2"));

        when(ruleRepository.findByRoomIdAndIsActiveTrue(roomId))
                .thenReturn(Arrays.asList(rule));

        // Act
        BigDecimal total = pricingRuleService.calculateTotalPrice(roomId, basePrice, checkIn, checkOut);

        // Assert: Night 1 (120.00) + Night 2 (100.00) = 220.00
        assertTrue(new BigDecimal("220.00").compareTo(total) == 0);
    }
}