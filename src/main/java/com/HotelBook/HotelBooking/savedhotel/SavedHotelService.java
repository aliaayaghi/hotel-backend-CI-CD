package com.HotelBook.HotelBooking.savedhotel;

import com.HotelBook.HotelBooking.catalog.policy.ConflictException;
import com.HotelBook.HotelBooking.catalog.user.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SavedHotelService {

    private final SavedHotelRepository savedHotelRepository;

    @Transactional(readOnly = true)
    public List<SavedHotelResponseDTO> getSavedHotels(UUID customerId) {
        return savedHotelRepository.findByCustomerIdOrderBySavedAtDesc(customerId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SavedHotelResponseDTO saveHotel(UUID customerId, UUID hotelId,
                                           SavedHotelRequestDTO request) {
        if (savedHotelRepository.existsByCustomerIdAndHotelId(customerId, hotelId)) {
            throw new ConflictException(
                    "Hotel is already in your wishlist. " +
                            "To update notes use PATCH /api/saved-hotels/" + hotelId + "/notes");
        }

        SavedHotel saved = savedHotelRepository.save(
                SavedHotel.builder()
                        .customerId(customerId)
                        .hotelId(hotelId)
                        .notes(normaliseNotes(request))
                        .build()
        );

        log.info("Customer {} saved hotel {}", customerId, hotelId);
        return toDTO(saved);
    }

    @Transactional
    public void unsaveHotel(UUID customerId, UUID hotelId) {
        // ── FIX: check existence before deleting so a second DELETE returns 404 ──
        if (!savedHotelRepository.existsByCustomerIdAndHotelId(customerId, hotelId)) {
            throw new ResourceNotFoundException("Hotel is not in your wishlist.", hotelId);
        }
        savedHotelRepository.deleteByCustomerIdAndHotelId(customerId, hotelId);
        log.info("Customer {} unsaved hotel {}", customerId, hotelId);
    }

    @Transactional(readOnly = true)
    public boolean isSaved(UUID customerId, UUID hotelId) {
        return savedHotelRepository.existsByCustomerIdAndHotelId(customerId, hotelId);
    }

    @Transactional
    public SavedHotelResponseDTO updateNotes(UUID customerId, UUID hotelId,
                                             SavedHotelRequestDTO request) {
        SavedHotel entity = savedHotelRepository
                .findByCustomerIdAndHotelId(customerId, hotelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hotel is not in your wishlist. " +
                                "Save it first via POST /api/saved-hotels/", hotelId));

        entity.setNotes(normaliseNotes(request));
        log.info("Customer {} updated notes for hotel {}", customerId, hotelId);
        return toDTO(savedHotelRepository.save(entity));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String normaliseNotes(SavedHotelRequestDTO req) {
        if (req == null || req.getNotes() == null || req.getNotes().isBlank()) return null;
        return req.getNotes().trim();
    }

    private SavedHotelResponseDTO toDTO(SavedHotel s) {
        return SavedHotelResponseDTO.builder()
                .id(s.getId())
                .customerId(s.getCustomerId())
                .hotelId(s.getHotelId())
                .notes(s.getNotes())
                .savedAt(s.getSavedAt())
                .build();
    }
}