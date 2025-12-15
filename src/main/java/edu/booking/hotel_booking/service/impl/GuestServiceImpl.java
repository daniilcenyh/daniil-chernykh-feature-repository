package edu.booking.hotel_booking.service.impl;

import edu.booking.hotel_booking.dao.GuestRepository;
import edu.booking.hotel_booking.dto.request.CreateGuestRequest;
import edu.booking.hotel_booking.dto.request.UpdateGuestRequest;
import edu.booking.hotel_booking.dto.response.GuestResponse;
import edu.booking.hotel_booking.entity.GuestEntity;
import edu.booking.hotel_booking.exception.GuestAlreadyExistException;
import edu.booking.hotel_booking.exception.GuestNotFoundException;
import edu.booking.hotel_booking.service.GuestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestServiceImpl implements GuestService {

    private final GuestRepository guestRepository;

    @Override
    @Transactional
    public GuestResponse createGuest(CreateGuestRequest request) {
        log.info("Starting creation of new guest with phone number: {}", request.phoneNumber());
        log.debug("Guest creation request details: firstName={}, lastName={}, birthDate={}",
                request.firstName(), request.lastName(), request.birthDate());

        if (validateGuestByPhoneNumber(request.phoneNumber())) {
            log.warn("Guest creation failed - phone number already exists: {}", request.phoneNumber());
            throw new GuestAlreadyExistException("Guest with this phone number: {%s} has been existed".formatted(request.phoneNumber()));
        }

        var guestToSave = GuestEntity.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .middleName(request.middleName())
                .phoneNumber(request.phoneNumber())
                .birthDate(request.birthDate())
                .createdAt(LocalDateTime.now())
                .build();

        log.debug("Attempting to save guest entity to database");
        var saved = this.guestRepository.save(guestToSave);
        log.info("Guest created successfully with ID: {}", saved.id());
        log.debug("Guest created with details: ID={}, fullName={} {}, phone={}",
                saved.id(), saved.firstName(), saved.lastName(), saved.phoneNumber());

        return GuestResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public GuestResponse updateGuest(Long id, UpdateGuestRequest request) {
        log.info("Starting update for guest with ID: {}", id);
        log.debug("Update request details for guest ID {}: firstName={}, lastName={}, phone={}",
                id, request.firstName(), request.lastName(), request.phoneNumber());

        var existedGuest = this.guestRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Guest not found for update - ID: {}", id);
                    return new GuestNotFoundException("Guest with id: {%s}, njt founded.".formatted(id.toString()));
                });
        log.debug("Found existing guest: ID={}, currentPhone={}", id, existedGuest.phoneNumber());

        if (request.phoneNumber() != null && !request.phoneNumber().equals(existedGuest.phoneNumber())) {
            if (validateGuestByPhoneNumber(request.phoneNumber())) {
                log.warn("Guest update failed - new phone number already exists: {}", request.phoneNumber());
                throw new GuestAlreadyExistException("Guest with this phone number: {%s} has been existed".formatted(request.phoneNumber()));
            }
            log.debug("Phone number will be updated from {} to {}", existedGuest.phoneNumber(), request.phoneNumber());
        }

        var guestToUpdate = GuestEntity.builder()
                .id(id)
                .firstName(request.firstName() != null ? request.firstName() : existedGuest.firstName())
                .lastName(request.lastName() != null ? request.lastName() : existedGuest.lastName())
                .middleName(request.middleName() != null ? request.middleName() : existedGuest.middleName())
                .phoneNumber(request.phoneNumber() != null ? request.phoneNumber() : existedGuest.phoneNumber())
                .birthDate(request.birthDate() != null ? request.birthDate() : existedGuest.birthDate())
                .build();

        log.debug("Attempting to update guest entity in database");
        var updated = this.guestRepository.save(guestToUpdate);
        log.info("Guest updated successfully - ID: {}", id);
        log.debug("Guest updated with new details: fullName={} {}, phone={}",
                updated.firstName(), updated.lastName(), updated.phoneNumber());

        return GuestResponse.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public GuestResponse getGuest(Long id) {
        log.debug("Fetching guest by ID: {}", id);

        var existedGuest = this.guestRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Guest not found - ID: {}", id);
                    return new GuestNotFoundException("Guest with id: {%s}, not found.".formatted(id.toString()));
                });

        log.info("Guest retrieved successfully - ID: {}, name: {} {}",
                id, existedGuest.firstName(), existedGuest.lastName());
        log.debug("Guest details: ID={}, phone={}, birthDate={}",
                existedGuest.id(), existedGuest.phoneNumber(), existedGuest.birthDate());

        return GuestResponse.fromEntity(existedGuest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GuestResponse> getAllGuests() {
        log.info("Fetching all guests");

        List<GuestEntity> guestEntities = this.guestRepository.findAll();

        log.info("Retrieved {} guests from database", guestEntities.size());
        log.debug("Guest IDs retrieved: {}",
                guestEntities.stream().map(GuestEntity::id).toList());

        return guestEntities.stream()
                .map(GuestResponse::fromEntity)
                .toList();
    }

    private boolean validateGuestByPhoneNumber(String phoneNUmber) {
        return this.guestRepository.existsByPhone(phoneNUmber);
    }
}
