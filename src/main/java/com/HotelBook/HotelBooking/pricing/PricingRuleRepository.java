package com.HotelBook.HotelBooking.pricing;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, UUID> {


    List<PricingRule> findByRoomIdAndIsActiveTrue(UUID roomId);

    List<PricingRule> findByRoomId(UUID roomId);

    List<PricingRule> findByRoomIdAndRuleType(UUID roomId, PricingRule.RuleType ruleType);

    @Query("SELECT r FROM PricingRule r " +
            "WHERE r.roomId = :roomId " +
            "AND r.isActive = true " +
            "AND r.ruleType IN ('SEASONAL', 'SPECIAL_EVENT') " +
            "AND r.startDate <= :rangeEnd " +
            "AND r.endDate >= :rangeStart")
    List<PricingRule> findOverlappingSeasonalRules(
            @Param("roomId")     UUID roomId,
            @Param("rangeStart") LocalDate rangeStart,
            @Param("rangeEnd")   LocalDate rangeEnd);

    void deleteByRoomId(UUID roomId);

    boolean existsByRoomIdAndIsActiveTrue(UUID roomId);
}
