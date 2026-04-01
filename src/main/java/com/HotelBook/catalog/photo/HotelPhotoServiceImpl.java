package com.HotelBook.catalog.photo;

import com.HotelBook.catalog.hotel.HotelRepository;
import com.HotelBook.catalog.user.exception.ResourceNotFoundException;
import com.HotelBook.catalog.user.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelPhotoServiceImpl implements HotelPhotoService {

    private final HotelPhotoRepository photoRepository;
    private final HotelRepository hotelRepository;
    private final HotelPhotoMapper photoMapper;

    // ── Get photos ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<HotelPhotoResponse> getPhotos(UUID hotelId) {
        verifyHotelExists(hotelId);
        List<HotelPhoto> photos = photoRepository.findByHotelIdOrderByOrderAsc(hotelId);
        return photoMapper.toResponseList(photos);
    }

    // ── Add photo ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public HotelPhotoResponse addPhoto(UUID hotelId, UUID managerId, CreatePhotoRequest request) {
        verifyHotelExists(hotelId);
        validateManagerOwnership(managerId, hotelId);

        // ── Cover logic ────────────────────────────────────────────────────────
        // If this photo is marked as cover, clear all existing covers first.
        // We use a bulk UPDATE rather than loading each photo individually.
        if (request.isCover()) {
            photoRepository.clearCoverByHotelId(hotelId);
            log.debug("Cleared existing cover for hotel {} before setting new cover", hotelId);
        }

        // Auto-assign display order: append at the end
        long photoCount = photoRepository.countByHotelId(hotelId);

        HotelPhoto photo = HotelPhoto.builder()
                .hotelId(hotelId)
                .url(request.getUrl())
                .caption(request.getCaption())
                .isCover(request.isCover())
                .order((int) photoCount)   // 0-based: new photo appended last
                .build();

        photo = photoRepository.save(photo);
        log.info("Added photo {} to hotel {} (cover={})", photo.getId(), hotelId, photo.isCover());

        return photoMapper.toResponse(photo);
    }

    // ── Delete photo ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deletePhoto(UUID hotelId, UUID photoId, UUID managerId) {
        verifyHotelExists(hotelId);
        validateManagerOwnership(managerId, hotelId);

        HotelPhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("HotelPhoto", photoId));

        // Verify the photo actually belongs to this hotel (prevents cross-hotel deletion)
        if (!photo.getHotelId().equals(hotelId)) {
            throw new ResourceNotFoundException("HotelPhoto", photoId);
        }

        photoRepository.delete(photo);
        log.info("Deleted photo {} from hotel {}", photoId, hotelId);

        // Optional: if deleted photo was the cover, the next photo (order=0) becomes
        // the de-facto first photo but no photo is marked as cover. The manager must
        // explicitly set a new cover. This is acceptable UX for Step 1.
    }

    // ── Reorder photos ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public List<HotelPhotoResponse> reorderPhotos(UUID hotelId, UUID managerId, ReorderPhotosRequest request) {
        verifyHotelExists(hotelId);
        validateManagerOwnership(managerId, hotelId);

        List<UUID> newOrder = request.getPhotoIds();

        // ── Validation: must include exactly all photos, no duplicates ──────────
        List<HotelPhoto> existingPhotos = photoRepository.findByHotelId(hotelId);

        if (existingPhotos.size() != newOrder.size()) {
            throw new IllegalArgumentException(
                    "photoIds must include all " + existingPhotos.size() + " photos for this hotel. " +
                            "Received " + newOrder.size() + " IDs."
            );
        }

        // Index existing photos by ID for O(1) lookup
        Map<UUID, HotelPhoto> photoById = existingPhotos.stream()
                .collect(Collectors.toMap(HotelPhoto::getId, Function.identity()));

        // Verify every submitted UUID belongs to this hotel
        for (UUID id : newOrder) {
            if (!photoById.containsKey(id)) {
                throw new ResourceNotFoundException("HotelPhoto", id);
            }
        }

        // ── Apply new ordering ─────────────────────────────────────────────────
        List<HotelPhoto> updated = new ArrayList<>();
        for (int i = 0; i < newOrder.size(); i++) {
            HotelPhoto photo = photoById.get(newOrder.get(i));
            photo.setOrder(i);
            updated.add(photo);
        }

        List<HotelPhoto> saved = photoRepository.saveAll(updated);
        log.info("Reordered {} photos for hotel {}", saved.size(), hotelId);

        // Return in the new order
        return saved.stream()
                .sorted((a, b) -> Integer.compare(a.getOrder(), b.getOrder()))
                .map(photoMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private void verifyHotelExists(UUID hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel", hotelId);
        }
    }

    /**
     * Verifies that the authenticated manager owns the given hotel.
     * Throws UnauthorizedException if the manager is not the owner.
     */
    private void validateManagerOwnership(UUID managerId, UUID hotelId) {
        if (!hotelRepository.existsByIdAndManager_Id(hotelId, managerId)) {
            throw new UnauthorizedException(
                    "You do not have permission to modify photos for hotel: " + hotelId
            );
        }
    }
}
