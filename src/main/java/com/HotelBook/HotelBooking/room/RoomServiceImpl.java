package com.HotelBook.HotelBooking.room;

import com.HotelBook.HotelBooking.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public RoomResponseDTO createRoom(UUID hotelId, RoomRequestDTO request) {
        Room room = Room.builder()
                .hotelId(hotelId)
                .name(request.getName())
                .type(request.getType())
                .bedType(request.getBedType())
                .description(request.getDescription())
                .maxAdults(request.getMaxAdults())
                .maxChildren(request.getMaxChildren())
                .quantity(request.getQuantity())
                .sizeSqm(request.getSizeSqm())
                .floor(request.getFloor())
                .view(request.getView() != null ? request.getView() : RoomView.NONE)
                .price(request.getPrice())
                .isActive(true)
                .build();

        Room saved = roomRepository.save(room);
        log.info("Room '{}' created for hotel {} with id {}", saved.getName(), hotelId, saved.getId());
        return toResponseDTO(saved);
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public RoomResponseDTO getRoomById(UUID hotelId, UUID roomId) {
        Room room = roomRepository.findByIdAndHotelId(roomId, hotelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room not found with id: " + roomId + " for hotel: " + hotelId));
        return toResponseDTO(room);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getRoomsByHotel(UUID hotelId) {
        return roomRepository.findByHotelIdAndIsActiveTrue(hotelId)
                .stream()
                .map(this::toListResponseDTO)   // ← uses list version (no collections)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getAllRoomsByHotel(UUID hotelId) {
        return roomRepository.findByHotelId(hotelId)
                .stream()
                .map(this::toListResponseDTO)   // ← uses list version (no collections)
                .collect(Collectors.toList());
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public RoomResponseDTO updateRoom(UUID hotelId, UUID roomId, RoomRequestDTO request) {
        Room room = roomRepository.findByIdAndHotelId(roomId, hotelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room not found with id: " + roomId + " for hotel: " + hotelId));

        if (request.getName() != null)        room.setName(request.getName());
        if (request.getType() != null)        room.setType(request.getType());
        if (request.getBedType() != null)     room.setBedType(request.getBedType());
        if (request.getDescription() != null) room.setDescription(request.getDescription());
        if (request.getMaxAdults() != null)   room.setMaxAdults(request.getMaxAdults());
        if (request.getMaxChildren() != null) room.setMaxChildren(request.getMaxChildren());
        if (request.getQuantity() != null)    room.setQuantity(request.getQuantity());
        if (request.getSizeSqm() != null)     room.setSizeSqm(request.getSizeSqm());
        if (request.getFloor() != null)       room.setFloor(request.getFloor());
        if (request.getView() != null)        room.setView(request.getView());
        if (request.getPrice() != null)       room.setPrice(request.getPrice());

        Room saved = roomRepository.save(room);
        log.info("Room {} updated", roomId);
        return toResponseDTO(saved);
    }

    // ── DELETE (SOFT) ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteRoom(UUID hotelId, UUID roomId) {
        Room room = roomRepository.findByIdAndHotelId(roomId, hotelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room not found with id: " + roomId + " for hotel: " + hotelId));
        room.deactivate();
        roomRepository.save(room);
        log.info("Room {} soft-deleted (deactivated)", roomId);
    }


    private RoomResponseDTO toResponseDTO(Room room) {
        if (room == null) return null;

        RoomResponseDTO dto = new RoomResponseDTO();
        setBaseFields(dto, room);

        // ── Photos ──────────────────────────────────────────────────────────
        dto.setPhotos(room.getPhotos() == null ? Collections.emptyList() :
                room.getPhotos().stream().map(p -> {
                    RoomResponseDTO.PhotoDTO d = new RoomResponseDTO.PhotoDTO();
                    d.setId(p.getId());
                    d.setUrl(p.getUrl());
                    d.setDisplayOrder(p.getDisplayOrder());
                    d.setCaption(p.getCaption());
                    return d;
                }).collect(Collectors.toList()));

        // ── Amenities ────────────────────────────────────────────────────────
        dto.setAmenities(room.getAmenities() == null ? Collections.emptyList() :
                room.getAmenities().stream().map(a -> {
                    RoomResponseDTO.AmenityDTO d = new RoomResponseDTO.AmenityDTO();
                    d.setId(a.getId());
                    d.setName(a.getName());
                    d.setCategory(a.getCategory().name());
                    d.setIcon(a.getIcon());
                    return d;
                }).collect(Collectors.toList()));

        // ── Accessibility ────────────────────────────────────────────────────
        dto.setAccessibilities(room.getAccessibilities() == null ? Collections.emptyList() :
                room.getAccessibilities().stream().map(a -> {
                    RoomResponseDTO.AccessibilityDTO d = new RoomResponseDTO.AccessibilityDTO();
                    d.setId(a.getId());
                    d.setFeature(a.getFeature());
                    d.setIsAvailable(a.getIsAvailable());
                    return d;
                }).collect(Collectors.toList()));

        // ── Pricing Rules (ACTIVE only — customers only see active rules) ────
        // NEW: maps Room.pricingRules via the new @OneToMany relationship
        dto.setPricingRules(room.getPricingRules() == null ? Collections.emptyList() :
                room.getPricingRules().stream()
                        .filter(r -> Boolean.TRUE.equals(r.getIsActive()))  // active rules only
                        .map(r -> {
                            RoomResponseDTO.PricingRuleDTO d = new RoomResponseDTO.PricingRuleDTO();
                            d.setId(r.getId());
                            d.setRuleType(r.getRuleType().name());
                            d.setStartDate(r.getStartDate());
                            d.setEndDate(r.getEndDate());
                            d.setDayOfWeek(r.getDayOfWeek());
                            d.setMultiplier(r.getMultiplier());
                            d.setPriority(r.getPriority());
                            d.setIsActive(r.getIsActive());
                            d.setDescription(r.getDescription());
                            return d;
                        }).collect(Collectors.toList()));

        // ── Cancellation Policies (room-specific tiers only) ─────────────────
        // NEW: maps Room.cancellationPolicies via the new @OneToMany relationship
        // NOTE: These are only the ROOM-SPECIFIC policies (roomId = this room).
        // Hotel-wide fallback policies are NOT included here — they are managed
        // separately via CancellationPolicyController.
        dto.setCancellationPolicies(room.getCancellationPolicies() == null ? Collections.emptyList() :
                room.getCancellationPolicies().stream().map(p -> {
                    RoomResponseDTO.CancellationPolicyDTO d = new RoomResponseDTO.CancellationPolicyDTO();
                    d.setId(p.getId());
                    d.setTierName(p.getTierName());
                    d.setDeadlineHours(p.getDeadlineHours());
                    d.setRefundPercentage(p.getRefundPercentage());
                    d.setPriceMultiplier(p.getPriceMultiplier());
                    d.setIsDefault(p.getIsDefault());
                    d.setDescription(p.getDescription());
                    return d;
                }).collect(Collectors.toList()));

        return dto;
    }


    private RoomResponseDTO toListResponseDTO(Room room) {
        if (room == null) return null;

        RoomResponseDTO dto = new RoomResponseDTO();
        setBaseFields(dto, room);

        // Omit all collections in list responses — set to empty to avoid null in JSON
        dto.setPhotos(Collections.emptyList());
        dto.setAmenities(Collections.emptyList());
        dto.setAccessibilities(Collections.emptyList());
        dto.setPricingRules(Collections.emptyList());
        dto.setCancellationPolicies(Collections.emptyList());

        return dto;
    }


    private void setBaseFields(RoomResponseDTO dto, Room room) {
        dto.setId(room.getId());
        dto.setHotelId(room.getHotelId());
        dto.setName(room.getName());
        dto.setType(room.getType());
        dto.setBedType(room.getBedType());
        dto.setDescription(room.getDescription());
        dto.setMaxAdults(room.getMaxAdults());
        dto.setMaxChildren(room.getMaxChildren());
        dto.setQuantity(room.getQuantity());
        dto.setSizeSqm(room.getSizeSqm());
        dto.setFloor(room.getFloor());
        dto.setView(room.getView());
        dto.setPrice(room.getPrice());
        dto.setIsActive(room.getIsActive());
        dto.setCreatedAt(room.getCreatedAt());
        dto.setUpdatedAt(room.getUpdatedAt());
    }
}