package com.HotelBook.HotelBooking;

import com.HotelBook.HotelBooking.Cancellation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancellationPolicyService Unit Tests")
class CancellationPolicyServiceTest {

    @Mock private CancellationPolicyRepository policyRepository;

    @InjectMocks
    private CancellationPolicyService policyService;

    private UUID hotelId;
    private UUID roomId;
    private UUID policyId;
    private CancellationPolicy freeCancelPolicy;

    @BeforeEach
    void setUp() {
        hotelId  = UUID.randomUUID();
        roomId   = UUID.randomUUID();
        policyId = UUID.randomUUID();

        freeCancelPolicy = CancellationPolicy.builder()
                .id(policyId)
                .hotelId(hotelId)
                .roomId(roomId)
                .tierName("Free Cancel 48h")
                .deadlineHours(48)
                .refundPercentage(100)
                .priceMultiplier(BigDecimal.ONE)
                .isDefault(true)
                .build();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getPoliciesForRoom
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getPoliciesForRoom()")
    class GetPoliciesForRoom {

        @Test
        @DisplayName("should return room-specific policies when they exist")
        void shouldReturnRoomSpecificPolicies() {
            given(policyRepository.findByRoomId(roomId)).willReturn(List.of(freeCancelPolicy));

            List<CancellationPolicyResponseDTO> result =
                    policyService.getPoliciesForRoom(hotelId, roomId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTierName()).isEqualTo("Free Cancel 48h");
        }

        @Test
        @DisplayName("should fall back to hotel-wide policies when no room-specific policy exists")
        void shouldFallBackToHotelWidePolicies() {
            given(policyRepository.findByRoomId(roomId)).willReturn(List.of());
            CancellationPolicy hotelWide = CancellationPolicy.builder()
                    .id(UUID.randomUUID())
                    .hotelId(hotelId)
                    .tierName("Hotel Default")
                    .deadlineHours(24)
                    .refundPercentage(50)
                    .priceMultiplier(BigDecimal.ONE)
                    .isDefault(true)
                    .build();
            given(policyRepository.findByHotelIdAndRoomIdIsNull(hotelId))
                    .willReturn(List.of(hotelWide));

            List<CancellationPolicyResponseDTO> result =
                    policyService.getPoliciesForRoom(hotelId, roomId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTierName()).isEqualTo("Hotel Default");
        }

        @Test
        @DisplayName("should return empty list when no policies exist at all")
        void shouldReturnEmptyWhenNoPolicies() {
            given(policyRepository.findByRoomId(roomId)).willReturn(List.of());
            given(policyRepository.findByHotelIdAndRoomIdIsNull(hotelId)).willReturn(List.of());

            List<CancellationPolicyResponseDTO> result =
                    policyService.getPoliciesForRoom(hotelId, roomId);

            assertThat(result).isEmpty();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getPolicyById
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getPolicyById()")
    class GetPolicyById {

        @Test
        @DisplayName("should return DTO when policy found")
        void shouldReturnPolicy() {
            given(policyRepository.findById(policyId)).willReturn(Optional.of(freeCancelPolicy));

            CancellationPolicyResponseDTO result = policyService.getPolicyById(policyId);

            assertThat(result.getId()).isEqualTo(policyId);
            assertThat(result.getDeadlineHours()).isEqualTo(48);
        }

        @Test
        @DisplayName("should throw RuntimeException when policy not found")
        void shouldThrowWhenNotFound() {
            given(policyRepository.findById(policyId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> policyService.getPolicyById(policyId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Cancellation policy not found");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  createRoomPolicy
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("createRoomPolicy()")
    class CreateRoomPolicy {

        @Test
        @DisplayName("should save a valid room policy")
        void shouldSaveValidPolicy() {
            CancellationPolicyRequestDTO request = buildRequest("Flex", 24, 100, BigDecimal.ONE, false);
            given(policyRepository.countByRoomId(roomId)).willReturn(0L);
            given(policyRepository.save(any())).willReturn(freeCancelPolicy);

            CancellationPolicyResponseDTO result =
                    policyService.createRoomPolicy(hotelId, roomId, request);

            assertThat(result).isNotNull();
            verify(policyRepository).save(any(CancellationPolicy.class));
        }

        @Test
        @DisplayName("should throw RuntimeException when room already has 5 tiers")
        void shouldThrowWhenMaxTiersReached() {
            given(policyRepository.countByRoomId(roomId)).willReturn(5L);
            CancellationPolicyRequestDTO request = buildRequest("Extra", 12, 50, BigDecimal.ONE, false);

            assertThatThrownBy(() -> policyService.createRoomPolicy(hotelId, roomId, request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("maximum of 5 cancellation tiers");
        }

        @Test
        @DisplayName("should throw RuntimeException for non-refundable tier with refund > 0")
        void shouldThrowForNonRefundableWithRefundPercentage() {
            given(policyRepository.countByRoomId(roomId)).willReturn(0L);
            // deadlineHours = 0 but refund = 50 — invalid
            CancellationPolicyRequestDTO request = buildRequest("Bad", 0, 50, BigDecimal.ONE, false);

            assertThatThrownBy(() -> policyService.createRoomPolicy(hotelId, roomId, request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Non-refundable tier");
        }

        @Test
        @DisplayName("should clear existing default when new policy is set as default")
        void shouldClearExistingDefaultWhenNewDefaultCreated() {
            given(policyRepository.countByRoomId(roomId)).willReturn(1L);
            CancellationPolicyRequestDTO request = buildRequest("New Default", 48, 100, BigDecimal.ONE, true);
            given(policyRepository.save(any())).willReturn(freeCancelPolicy);

            policyService.createRoomPolicy(hotelId, roomId, request);

            verify(policyRepository).clearDefaultForRoom(roomId);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  deleteRoomPolicy
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("deleteRoomPolicy()")
    class DeleteRoomPolicy {

        @Test
        @DisplayName("should delete policy when found for room")
        void shouldDeletePolicy() {
            given(policyRepository.findByIdAndRoomId(policyId, roomId))
                    .willReturn(Optional.of(freeCancelPolicy));

            policyService.deleteRoomPolicy(roomId, policyId);

            verify(policyRepository).delete(freeCancelPolicy);
        }

        @Test
        @DisplayName("should throw RuntimeException when policy not found for room")
        void shouldThrowWhenNotFound() {
            given(policyRepository.findByIdAndRoomId(policyId, roomId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> policyService.deleteRoomPolicy(roomId, policyId))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  calculateRefund (delegated to entity)
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("calculateRefund()")
    class CalculateRefund {

        @Test
        @DisplayName("should return full refund when cancelled well before deadline")
        void shouldReturnFullRefundBeforeDeadline() {
            given(policyRepository.findById(policyId)).willReturn(Optional.of(freeCancelPolicy));

            LocalDateTime checkIn     = LocalDateTime.now().plusDays(3);
            LocalDateTime cancelledAt = LocalDateTime.now();            // 72h before

            BigDecimal refund = policyService.calculateRefund(
                    policyId, cancelledAt, checkIn, new BigDecimal("200.00"));

            assertThat(refund).isEqualByComparingTo("200.00");
        }

        @Test
        @DisplayName("should return zero refund when cancelled after deadline")
        void shouldReturnZeroRefundAfterDeadline() {
            given(policyRepository.findById(policyId)).willReturn(Optional.of(freeCancelPolicy));

            LocalDateTime checkIn     = LocalDateTime.now().plusHours(10); // only 10h away
            LocalDateTime cancelledAt = LocalDateTime.now();

            BigDecimal refund = policyService.calculateRefund(
                    policyId, cancelledAt, checkIn, new BigDecimal("200.00"));

            assertThat(refund).isEqualByComparingTo("0.00");
        }
    }

    // ─── CancellationPolicy entity pure-logic tests ────────────────────────
    @Nested
    @DisplayName("CancellationPolicy entity logic")
    class PolicyEntityLogic {

        @Test
        @DisplayName("isNonRefundable() returns true when deadlineHours=0 and refundPercentage=0")
        void shouldBeNonRefundable() {
            CancellationPolicy p = CancellationPolicy.builder()
                    .deadlineHours(0).refundPercentage(0).build();
            assertThat(p.isNonRefundable()).isTrue();
        }

        @Test
        @DisplayName("isNonRefundable() returns false when deadlineHours > 0")
        void shouldNotBeNonRefundableWhenDeadlineSet() {
            CancellationPolicy p = CancellationPolicy.builder()
                    .deadlineHours(24).refundPercentage(100).build();
            assertThat(p.isNonRefundable()).isFalse();
        }

        @Test
        @DisplayName("calculateRefund() returns ZERO for non-refundable policy regardless of timing")
        void shouldReturnZeroForNonRefundablePolicy() {
            CancellationPolicy p = CancellationPolicy.builder()
                    .deadlineHours(0).refundPercentage(0)
                    .priceMultiplier(BigDecimal.ONE).build();

            BigDecimal refund = p.calculateRefund(
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(10),
                    new BigDecimal("500.00"));

            assertThat(refund).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("getAdjustedPrice() applies priceMultiplier correctly")
        void shouldApplyPriceMultiplier() {
            CancellationPolicy p = CancellationPolicy.builder()
                    .deadlineHours(24).refundPercentage(0)
                    .priceMultiplier(new BigDecimal("1.15")).build();

            BigDecimal adjusted = p.getAdjustedPrice(new BigDecimal("100.00"));

            assertThat(adjusted).isEqualByComparingTo("115.00");
        }

        @Test
        @DisplayName("isEligibleForRefund() returns false when deadline is 0")
        void shouldNotBeEligibleForRefundWhenNonRefundable() {
            CancellationPolicy p = CancellationPolicy.builder()
                    .deadlineHours(0).refundPercentage(0).build();

            assertThat(p.isEligibleForRefund(LocalDateTime.now(), LocalDateTime.now().plusDays(5)))
                    .isFalse();
        }

        @Test
        @DisplayName("isEligibleForRefund() returns true when cancelled before deadline")
        void shouldBeEligibleWhenCancelledBeforeDeadline() {
            CancellationPolicy p = CancellationPolicy.builder()
                    .deadlineHours(48).refundPercentage(100).build();

            LocalDateTime cancelledAt = LocalDateTime.now();
            LocalDateTime checkIn     = LocalDateTime.now().plusDays(3); // 72h ahead

            assertThat(p.isEligibleForRefund(cancelledAt, checkIn)).isTrue();
        }

        @Test
        @DisplayName("isEligibleForRefund() returns false when cancelled after deadline")
        void shouldNotBeEligibleWhenCancelledAfterDeadline() {
            CancellationPolicy p = CancellationPolicy.builder()
                    .deadlineHours(48).refundPercentage(100).build();

            LocalDateTime cancelledAt = LocalDateTime.now();
            LocalDateTime checkIn     = LocalDateTime.now().plusHours(10); // only 10h

            assertThat(p.isEligibleForRefund(cancelledAt, checkIn)).isFalse();
        }
    }

    // ─── Helpers ────────────────────────────────────────────────────────────
    private CancellationPolicyRequestDTO buildRequest(String name, int deadline, int refund,
                                                      BigDecimal multiplier, boolean isDefault) {
        CancellationPolicyRequestDTO dto = new CancellationPolicyRequestDTO();
        dto.setTierName(name);
        dto.setDeadlineHours(deadline);
        dto.setRefundPercentage(refund);
        dto.setPriceMultiplier(multiplier);
        dto.setIsDefault(isDefault);
        return dto;
    }
}
