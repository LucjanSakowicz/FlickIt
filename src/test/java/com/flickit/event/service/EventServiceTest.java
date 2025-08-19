package com.flickit.event.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flickit.event.dto.CreateEventRequest;
import com.flickit.event.dto.EventDto;
import com.flickit.event.model.EventEntity;
import com.flickit.event.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class EventServiceTest {

    @MockBean
    private EventRepository eventRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EventService eventService;

    @Test
    void getAllEvents_shouldReturnEventDtos() {
        // given
        EventEntity entity = new EventEntity();
        List<EventEntity> entities = List.of(entity);
        when(eventRepository.findAll()).thenReturn(entities);

        // when
        List<EventDto> result = eventService.getAllEvents();

        // then
        assertEquals(1, result.size());
        assertNotNull(result.get(0));
        verify(eventRepository).findAll();
    }

    @Test
    void createEvent_shouldSaveAndReturnEventDto() {
        // given
        CreateEventRequest request = new CreateEventRequest();
        request.setTitleVendor("Test Event");
        request.setLat(50.0);
        request.setLon(20.0);
        request.setCategory(EventEntity.Category.OTHER);
        request.setExpiresAt(Instant.now().plusSeconds(3600));
        
        UUID vendorId = UUID.randomUUID();
        EventEntity saved = new EventEntity();
        when(eventRepository.save(any(EventEntity.class))).thenReturn(saved);

        // when
        EventDto result = eventService.createEvent(request, vendorId);

        // then
        assertNotNull(result);
        verify(eventRepository).save(any(EventEntity.class));
    }

    @Test
    void getEventById_shouldReturnEventDtoIfExists() {
        // given
        UUID id = UUID.randomUUID();
        EventEntity entity = new EventEntity();
        when(eventRepository.findById(id)).thenReturn(Optional.of(entity));

        // when
        EventDto result = eventService.getEventById(id);

        // then
        assertNotNull(result);
        verify(eventRepository).findById(id);
    }

    @Test
    void getEventById_shouldReturnNullIfNotExists() {
        // given
        UUID id = UUID.randomUUID();
        when(eventRepository.findById(id)).thenReturn(Optional.empty());

        // when
        EventDto result = eventService.getEventById(id);

        // then
        assertNull(result);
        verify(eventRepository).findById(id);
    }
} 