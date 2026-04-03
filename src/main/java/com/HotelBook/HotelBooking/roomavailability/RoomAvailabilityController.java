package com.HotelBook.HotelBooking.roomavailability;



import com.hotelapp.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
public class RoomAvailabilityController {

    private final RoomAvailabilityService availabilityService;


    @GetMapping("/api/rooms/{roomId}/availability")
    public ResponseEntity<ApiResponse<List<AvailabilitySummaryResponseDTO>>> getAvailability(
            @PathVariable UUID roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam int roomQuantity) {

        if (!to.isAfter(from)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("'to' must be after 'from'."));
        }

        List<AvailabilitySummaryResponseDTO> summary =
                availabilityService.getAvailabilitySummary(roomId, from, to, roomQuantity);

        return ResponseEntity.ok(ApiResponse.success(
                "Availability retrieved for " + summary.size() + " dates.", summary));
    }


    @GetMapping("/api/hotels/{hotelId}/rooms/{roomId}/availability")
    public ResponseEntity<ApiResponse<List<AvailabilitySummaryResponseDTO>>> getAvailabilityNested(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam int roomQuantity) {

        // Delegate to the flat path — hotelId is intentionally unused (rooms have unique UUIDs)
        return getAvailability(roomId, from, to, roomQuantity);
    }

    @GetMapping("/api/rooms/{roomId}/availability/blocked")
    public ResponseEntity<ApiResponse<List<RoomAvailabilityResponseDTO>>> getBlockedDates(
            @PathVariable UUID roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        if (!to.isAfter(from)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("'to' must be after 'from'."));
        }

        List<RoomAvailabilityResponseDTO> blocked =
                availabilityService.getBlockedDates(roomId, from, to);

        return ResponseEntity.ok(ApiResponse.success(
                blocked.size() + " blocked date(s) found.", blocked));
    }


    @PostMapping("/api/rooms/{roomId}/availability/block")
    public ResponseEntity<ApiResponse<List<AvailabilitySummaryResponseDTO>>> blockDates(
            @PathVariable UUID roomId,
            @RequestParam int roomQuantity,
            @Valid @RequestBody RoomAvailabilityRequestDTO request) {

        List<AvailabilitySummaryResponseDTO> result =
                availabilityService.manualBlock(roomId, roomQuantity, request);

        return ResponseEntity.ok(ApiResponse.success(
                "Dates blocked from " + request.getFromDate() + " to " + request.getToDate()
                        + " (reason: " + request.getReason() + ").", result));
    }

    @DeleteMapping("/api/rooms/{roomId}/availability/unblock")
    public ResponseEntity<ApiResponse<List<AvailabilitySummaryResponseDTO>>> unblockDates(
            @PathVariable UUID roomId,
            @RequestParam int roomQuantity,
            @Valid @RequestBody RoomAvailabilityRequestDTO request) {

        List<AvailabilitySummaryResponseDTO> result =
                availabilityService.manualUnblock(roomId, roomQuantity, request);

        return ResponseEntity.ok(ApiResponse.success(
                "Dates unblocked from " + request.getFromDate() + " to " + request.getToDate() + ".",
                result));
    }
}