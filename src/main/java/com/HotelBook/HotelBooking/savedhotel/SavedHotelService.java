package com.HotelBook.HotelBooking.savedhotel;


import com.HotelBook.HotelBooking.common.ResourceNotFoundException;
import com.hotelapp.common.ConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class SavedHotelService {

    private final SavedHotelRepository savedHotelRepository;

    @Transactional
    public SavedHotelResponseDTO saveHotel(UUID customerId, UUID hotelId,
                                           SavedHotelRequestDTO request) {
        if (savedHotelRepository.existsByCustomerIdAndHotelId(customerId, hotelId)) {
            throw new ConflictException(
                    "Hotel is already in your wishlist. " +
                            "To update your note, use PATCH /api/saved-hotels/" + hotelId + "/notes");
        }

        SavedHotel saved = SavedHotel.builder()
                .customerId(customerId)
                .hotelId(hotelId)
                .notes(normaliseNotes(request != null ? request.getNotes() : null))
                .build();

        SavedHotel result = savedHotelRepository.save(saved);
        log.info("Customer {} saved hotel {}", customerId, hotelId);
        return toResponseDTO(result);
    }


    @Transactional
    public void unsaveHotel(UUID customerId, UUID hotelId) {
        if (!savedHotelRepository.existsByCustomerIdAndHotelId(customerId, hotelId)) {
            throw new ResourceNotFoundException(
                    "Hotel is not in your wishlist.");
        }
        savedHotelRepository.deleteByCustomerIdAndHotelId(customerId, hotelId);
        log.info("Customer {} unsaved hotel {}", customerId, hotelId);
    }

    @Transactional(readOnly = true)
    public List<SavedHotelResponseDTO> getSavedHotels(UUID customerId) {
        return savedHotelRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isSaved(UUID customerId, UUID hotelId) {
        return savedHotelRepository.existsByCustomerIdAndHotelId(customerId, hotelId);
    }


    @Transactional
    public SavedHotelResponseDTO updateNotes(UUID customerId, UUID hotelId,
                                             SavedHotelRequestDTO request) {
        SavedHotel savedHotel = savedHotelRepository
                .findByCustomerIdAndHotelId(customerId, hotelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hotel is not in your wishlist. Save it first via POST /api/saved-hotels/" + hotelId));

        savedHotel.setNotes(normaliseNotes(request.getNotes()));
        SavedHotel updated = savedHotelRepository.save(savedHotel);
        log.info("Customer {} updated notes for hotel {}", customerId, hotelId);
        return toResponseDTO(updated);
    }

   private String normaliseNotes(String notes) {
        if (notes == null || notes.isBlank()) return null;
        return notes.trim();
    }

    private SavedHotelResponseDTO toResponseDTO(SavedHotel saved) {
        SavedHotelResponseDTO dto = new SavedHotelResponseDTO();
        dto.setId(saved.getId());
        dto.setCustomerId(saved.getCustomerId());
        dto.setHotelId(saved.getHotelId());
        dto.setNotes(saved.getNotes());
        dto.setSavedAt(saved.getCreatedAt());
        return dto;
    }
}
