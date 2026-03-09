package com.HotelBook.HotelBooking.booking;


import com.HotelBook.HotelBooking.cancellation.CancellationPolicy;
import com.HotelBook.HotelBooking.cancellation.CancellationPolicyService;
import com.HotelBook.HotelBooking.pricing.PricingRuleService;
import com.HotelBook.HotelBooking.room.Room;
import com.HotelBook.HotelBooking.room.RoomRepository;
import com.HotelBook.HotelBooking.roomavailability.RoomAvailabilityService;
import com.hotelapp.common.BadRequestException;
import com.hotelapp.common.ConflictException;
import com.hotelapp.common.ResourceNotFoundException;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final RoomAvailabilityService availabilityService;
    private final PricingRuleService pricingService;
    private final CancellationPolicyService cancellationPolicyService;
    private final PaymentRepository paymentRepository;
    private final NotificationPort notificationPort;

    @Transactional
    public BookingResponseDTO createBooking(UUID customerId, BookingRequestDTO request) {

        // ── Step 1: Date validation ─────────────────────────────────────────
        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new BadRequestException(
                    "Check-out date must be after check-in date. " +
                            "Got: checkIn=" + request.getCheckInDate() + ", checkOut=" + request.getCheckOutDate());
        }

       Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", request.getRoomId()));

        if (!Boolean.TRUE.equals(room.getIsActive())) {
            throw new ConflictException("Room is not currently available for booking.");
        }

        // Verify room belongs to the stated hotel
        if (!room.getHotelId().equals(request.getHotelId())) {
            throw new BadRequestException(
                    "Room " + request.getRoomId() + " does not belong to hotel " + request.getHotelId());
        }

        int adults = request.getAdults();
        int children = request.getChildren() != null ? request.getChildren() : 0;

        if (!room.canAccommodate(adults, children)) {
            throw new BadRequestException(
                    "Room capacity exceeded. Room allows max " + room.getMaxAdults() +
                            " adults and " + room.getMaxChildren() + " children. " +
                            "Requested: " + adults + " adults, " + children + " children.");
        }

       int roomCount = request.getRoomCount() != null ? request.getRoomCount() : 1;

        boolean available = availabilityService.isAvailable(
                room.getId(),
                request.getCheckInDate(),
                request.getCheckOutDate(),
                roomCount,
                room.getQuantity()
        );

        if (!available) {
            throw new ConflictException(
                    "Room is not available for the selected dates: " +
                            request.getCheckInDate() + " to " + request.getCheckOutDate() +
                            ". Please choose different dates or a different room.");
        }

      BigDecimal nightlySubtotal = pricingService.calculateTotalPrice(
                room.getId(),
                room.getPrice(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        // 5b: Determine price per night (average for display purposes)
        long numberOfNights = request.getCheckInDate().until(request.getCheckOutDate()).getDays();
        BigDecimal pricePerNight = numberOfNights > 0
                ? nightlySubtotal.divide(BigDecimal.valueOf(numberOfNights), 2, RoundingMode.HALF_UP)
                : room.getPrice();

        // 5c: Apply cancellationPolicy.priceMultiplier
        UUID policyId = null;
        BigDecimal policyMultiplier = BigDecimal.ONE;

        if (request.getCancellationPolicyId() != null) {
            // Customer specified a policy ID
            CancellationPolicy policy = cancellationPolicyService
                    .getPolicyEntityById(request.getCancellationPolicyId());
            policyId = policy.getId();
            policyMultiplier = policy.getPriceMultiplier();

        } else {
            // Auto-select the default policy for this room/hotel
            var policies = cancellationPolicyService.getPoliciesForRoom(
                    request.getHotelId(), request.getRoomId());

            Optional<com.HotelBook.HotelBooking.cancellation.CancellationPolicyResponseDTO> defaultPolicy =
                    policies.stream().filter(p -> Boolean.TRUE.equals(p.getIsDefault())).findFirst();

            if (defaultPolicy.isEmpty() && !policies.isEmpty()) {
                defaultPolicy = Optional.of(policies.get(0)); // first available
            }

            if (defaultPolicy.isPresent()) {
                policyId = defaultPolicy.get().getId();
                policyMultiplier = defaultPolicy.get().getPriceMultiplier();
            }
        }

        // 5d: Final total = nightly subtotal × roomCount × policyMultiplier
        BigDecimal totalPrice = nightlySubtotal
                .multiply(BigDecimal.valueOf(roomCount))
                .multiply(policyMultiplier)
                .setScale(2, RoundingMode.HALF_UP);

        // ── Step 6: Save booking ─────────────────────────────────────────────
        Booking booking = Booking.builder()
                .customerId(customerId)
                .hotelId(request.getHotelId())
                .roomId(room.getId())
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .adults(adults)
                .children(children)
                .roomCount(roomCount)
                .pricePerNight(pricePerNight)
                .totalPrice(totalPrice)
                .cancellationPolicyId(policyId)
                .status(BookingStatus.PENDING)
                .specialRequests(request.getSpecialRequests())
                .build();

        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} created for customer {} — room {}, {} nights, total {}",
                saved.getId(), customerId, room.getId(), numberOfNights, totalPrice);

        return toResponseDTO(saved);
    }


    @Transactional
    public void confirmBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ConflictException(
                    "Cannot confirm booking " + bookingId + " — current status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        // Block the dates NOW that payment is confirmed
        availabilityService.blockDates(
                booking.getRoomId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getRoomCount(),
                booking.getId()
        );

        // Send confirmation notification (stub logs in dev, real email in prod)
        try {
            notificationPort.sendBookingConfirmation(
                    booking.getCustomerId(),
                    booking.getId(),
                    "Hotel"  // Replace with actual hotel name from Member 1's hotel data on merge
            );
        } catch (Exception e) {
            // Notification failure must NEVER roll back the booking confirmation
            log.warn("Failed to send confirmation notification for booking {}: {}",
                    bookingId, e.getMessage());
        }

        log.info("Booking {} CONFIRMED — dates blocked {}-{}",
                bookingId, booking.getCheckInDate(), booking.getCheckOutDate());
    }


    @Transactional
    public void markFailed(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        booking.setStatus(BookingStatus.FAILED);
        bookingRepository.save(booking);

        try {
            notificationPort.sendPaymentFailed(booking.getCustomerId(), booking.getId());
        } catch (Exception e) {
            log.warn("Failed to send payment failure notification for booking {}: {}",
                    bookingId, e.getMessage());
        }

        log.info("Booking {} marked as FAILED (payment unsuccessful)", bookingId);
    }


    @Transactional
    public BookingResponseDTO cancelBooking(UUID bookingId, UUID customerId, String cancelledBy) {
        Booking booking = bookingRepository.findByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (!booking.isCancellable()) {
            throw new ConflictException(
                    "Cannot cancel booking with status: " + booking.getStatus() +
                            ". Only PENDING or CONFIRMED bookings can be cancelled.");
        }

        boolean wasConfirmed = booking.getStatus() == BookingStatus.CONFIRMED;

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancelledBy(cancelledBy);
        bookingRepository.save(booking);

 if (wasConfirmed) {
            availabilityService.unblockDates(
                    booking.getRoomId(),
                    booking.getCheckInDate(),
                    booking.getCheckOutDate(),
                    booking.getRoomCount()
            );
            log.info("Dates unblocked for cancelled booking {}", bookingId);
        }

        log.info("Booking {} CANCELLED by {} (was: {})", bookingId, cancelledBy,
                wasConfirmed ? "CONFIRMED" : "PENDING");

        return toResponseDTO(booking);
    }


    @Transactional
    public BookingResponseDTO completeBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ConflictException(
                    "Can only complete a CONFIRMED booking. Current status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);

        log.info("Booking {} marked as COMPLETED", bookingId);
        return toResponseDTO(booking);
    }


    @Transactional
    public BookingResponseDTO markNoShow(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ConflictException(
                    "Can only mark NO_SHOW for a CONFIRMED booking. Current status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.NO_SHOW);
        bookingRepository.save(booking);

        log.info("Booking {} marked as NO_SHOW", bookingId);
        return toResponseDTO(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponseDTO getBookingById(UUID bookingId, UUID customerId) {
        Booking booking = bookingRepository.findByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        return toResponseDTO(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getBookingsByCustomer(UUID customerId) {
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getBookingsByHotel(UUID hotelId) {
        return bookingRepository.findByHotelIdOrderByCheckInDateAsc(hotelId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }


    private BookingResponseDTO toResponseDTO(Booking booking) {
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(booking.getId());
        dto.setCustomerId(booking.getCustomerId());
        dto.setHotelId(booking.getHotelId());
        dto.setRoomId(booking.getRoomId());
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setAdults(booking.getAdults());
        dto.setChildren(booking.getChildren());
        dto.setRoomCount(booking.getRoomCount());
        dto.setNumberOfNights(booking.getNumberOfNights());
        dto.setPricePerNight(booking.getPricePerNight());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setStatus(booking.getStatus());
        dto.setSpecialRequests(booking.getSpecialRequests());
        dto.setCancelledAt(booking.getCancelledAt());
        dto.setCancelledBy(booking.getCancelledBy());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());

        paymentRepository.findByBookingId(booking.getId()).ifPresent(payment -> {
            BookingResponseDTO.PaymentSummary ps = new BookingResponseDTO.PaymentSummary();
            ps.setPaymentId(payment.getId());
            ps.setStatus(payment.getStatus().name());
            ps.setAmount(payment.getAmount());
            ps.setRefundAmount(payment.getRefundAmount());
            ps.setPaymentMethod(payment.getPaymentMethod());
            ps.setPaidAt(payment.getPaidAt());
            dto.setPayment(ps);
        });

       if (booking.getCancellationPolicyId() != null) {
            try {
                var policyDTO = cancellationPolicyService.getPolicyById(booking.getCancellationPolicyId());
                BookingResponseDTO.CancellationTierSummary cs = new BookingResponseDTO.CancellationTierSummary();
                cs.setPolicyId(policyDTO.getId());
                cs.setTierName(policyDTO.getTierName());
                cs.setDeadlineHours(policyDTO.getDeadlineHours());
                cs.setRefundPercentage(policyDTO.getRefundPercentage());
                cs.setPriceMultiplier(policyDTO.getPriceMultiplier());
                dto.setCancellationPolicy(cs);
            } catch (Exception e) {
                log.debug("Could not load cancellation policy {} for booking {}",
                        booking.getCancellationPolicyId(), booking.getId());
            }
        }

        return dto;
    }
}