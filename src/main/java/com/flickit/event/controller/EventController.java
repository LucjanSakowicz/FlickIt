package com.flickit.event.controller;

import com.flickit.auth.model.CurrentUser;
import com.flickit.auth.service.AuthContext;
import com.flickit.claim.service.ClaimService;
import com.flickit.event.dto.CreateEventRequest;
import com.flickit.event.dto.EventDto;
import com.flickit.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Event management and discovery")
public class EventController {

    private final EventService eventService;
    private final ClaimService claimService;

    @GetMapping
    @Operation(summary = "List all events")
    public List<EventDto> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/latest")
    @Operation(summary = "Find events near location")
    public List<EventDto> getLatestEvents(
            @Parameter(description = "Latitude") @RequestParam double lat,
            @Parameter(description = "Longitude") @RequestParam double lon,
            @Parameter(description = "Radius in meters", example = "2000") @RequestParam(defaultValue = "2000") double radiusMeters) {
        return eventService.getEventsByLocation(lat, lon, radiusMeters);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by id")
    public ResponseEntity<EventDto> getEventById(@PathVariable UUID id) {
        EventDto dto = eventService.getEventById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Create new event")
    public ResponseEntity<EventDto> createEvent(@RequestBody @Valid CreateEventRequest request) {
        CurrentUser currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(eventService.createEvent(request, currentUser.getId()));
    }

    @PutMapping("/{id}/claim")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Claim an event")
    public ResponseEntity<Void> claimEvent(@PathVariable UUID id) {
        CurrentUser currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        
        boolean success = claimService.claimEvent(id, currentUser.getId());
        return success ? ResponseEntity.ok().build() : ResponseEntity.status(409).build();
    }
}
