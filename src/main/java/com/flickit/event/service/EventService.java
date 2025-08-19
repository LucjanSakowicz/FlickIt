package com.flickit.event.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flickit.event.dto.CreateEventRequest;
import com.flickit.event.dto.EventDto;
import com.flickit.event.model.EventEntity;
import com.flickit.event.repository.EventRepository;
import com.flickit.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public List<EventDto> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(entity -> objectMapper.convertValue(entity, EventDto.class))
                .collect(Collectors.toList());
    }

    public EventDto createEvent(CreateEventRequest request, UUID vendorId) {
        // For MVP - mock AI title/description generation
        String aiTitle = "AI: " + request.getTitleVendor();
        String aiDescription = "AI generated: " + (request.getDescriptionVendor() != null ? 
            request.getDescriptionVendor() : "Great offer!");

        // Create entity using ObjectMapper and then set additional fields
        EventEntity entity = new EventEntity();
        // Since EventEntity only has @Getter, we need to use reflection or create a new entity
        // For now, let's create a minimal entity and use ObjectMapper for conversion
        EventDto tempDto = new EventDto();
        tempDto.setTitleAi(aiTitle);
        tempDto.setTitleVendor(request.getTitleVendor());
        tempDto.setDescriptionAi(aiDescription);
        tempDto.setDescriptionVendor(request.getDescriptionVendor());
        tempDto.setLat(request.getLat());
        tempDto.setLon(request.getLon());
        tempDto.setAlt(request.getAlt());
        tempDto.setFloor(request.getFloor());
        tempDto.setCategory(request.getCategory());
        tempDto.setExpiresAt(request.getExpiresAt());
        tempDto.setStatus(EventEntity.Status.ACTIVE);
        tempDto.setVendorId(vendorId);
        
        entity = objectMapper.convertValue(tempDto, EventEntity.class);

        EventEntity saved = eventRepository.save(entity);
        EventDto result = convertToDto(saved);
        
        // Send notification to subscribers in radius
        notificationService.sendEventCreatedNotification(request.getLat(), request.getLon(), request.getTitleVendor());
        
        return result;
    }

    public List<EventDto> getEventsByLocation(double lat, double lon, double radiusMeters) {
        // For MVP - simple implementation without actual geo-spatial queries
        // In production this would use PostGIS or similar
        return eventRepository.findAll().stream()
                .filter(event -> {
                    double distance = calculateDistance(lat, lon, event.getLat(), event.getLon());
                    return distance <= radiusMeters;
                })
                .filter(event -> event.getStatus() == EventEntity.Status.ACTIVE)
                .filter(event -> event.getExpiresAt().isAfter(Instant.now()))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public EventDto getEventById(UUID id) {
        return eventRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    private EventDto convertToDto(EventEntity entity) {
        EventDto dto = objectMapper.convertValue(entity, EventDto.class);
        // Set computed fields
        dto.setTitle(entity.getTitleVendor() != null ? entity.getTitleVendor() : entity.getTitleAi());
        dto.setDescription(entity.getDescriptionVendor() != null ? entity.getDescriptionVendor() : entity.getDescriptionAi());
        return dto;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula for calculating distance between two points on Earth
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        return distance;
    }
}
