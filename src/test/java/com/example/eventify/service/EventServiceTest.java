package com.example.eventify.service;

import com.example.eventify.dto.EventCreateDTO;
import com.example.eventify.dto.EventResponseDTO;
import com.example.eventify.exception.BusinessRuleViolationException;
import com.example.eventify.exception.ResourceNotFoundException;
import com.example.eventify.mapper.EventMapper;
import com.example.eventify.model.Category;
import com.example.eventify.model.Event;
import com.example.eventify.model.Venue;
import com.example.eventify.repository.CategoryRepository;
import com.example.eventify.repository.EventRepository;
import com.example.eventify.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * EventServiceTest - Unit tests for EventService with DTO architecture
 */
@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventService eventService;

    private EventCreateDTO validEventDTO;
    private Event eventEntity;
    private Venue venue;
    private EventResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        validEventDTO = new EventCreateDTO();
        validEventDTO.setNombre("Concierto de Rock");
        validEventDTO.setFecha(LocalDate.of(2026, 12, 25));
        validEventDTO.setDescripcion("Un emocionante concierto de las mejores bandas de rock");
        validEventDTO.setVenueId(1L);
        validEventDTO.setCategoryIds(new HashSet<>());

        venue = new Venue();
        venue.setId(1L);
        venue.setNombre("Auditorio Nacional");
        venue.setDireccion("Calle Principal 123");
        venue.setCapacidad(5000);
        venue.setCiudad("Bogotá");

        eventEntity = new Event();
        eventEntity.setId(1L);
        eventEntity.setNombre("Concierto de Rock");
        eventEntity.setFecha(LocalDate.of(2026, 12, 25));
        eventEntity.setDescripcion("Un emocionante concierto de las mejores bandas de rock");
        eventEntity.setVenue(venue);
        eventEntity.setCategories(new HashSet<>());
        eventEntity.setIsActive(true);

        responseDTO = new EventResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setNombre("Concierto de Rock");
        responseDTO.setFecha(LocalDate.of(2026, 12, 25));
        responseDTO.setDescripcion("Un emocionante concierto");
        responseDTO.setVenueName("Auditorio Nacional");
        responseDTO.setCategoryNames(new HashSet<>());
    }

    @Test
    void deberiaGuardarEventoValido() {
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        when(eventMapper.toEntity(validEventDTO)).thenReturn(eventEntity);
        when(eventRepository.save(any(Event.class))).thenReturn(eventEntity);
        when(eventMapper.toResponseDTO(eventEntity)).thenReturn(responseDTO);

        EventResponseDTO resultado = eventService.crear(validEventDTO);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Concierto de Rock", resultado.getNombre());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void deberiaRechazarEventoSinVenue() {
        validEventDTO.setVenueId(null);

        assertThrows(BusinessRuleViolationException.class, () -> eventService.crear(validEventDTO));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void deberiaRechazarEventoConVenueNoExistente() {
        when(venueRepository.findById(999L)).thenReturn(Optional.empty());
        validEventDTO.setVenueId(999L);

        assertThrows(ResourceNotFoundException.class, () -> eventService.crear(validEventDTO));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void deberiaObtenerEventoPorId() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(eventEntity));
        when(eventMapper.toResponseDTO(eventEntity)).thenReturn(responseDTO);

        var resultado = eventService.obtenerPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Concierto de Rock", resultado.get().getNombre());
    }

    @Test
    void deberiaRetornarVacioAlObtenerEventoNoExistente() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        var resultado = eventService.obtenerPorId(999L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deberiaSoftDeletearEvento() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(eventEntity));

        eventService.softDeletear(1L);

        assertFalse(eventEntity.getIsActive());
        verify(eventRepository).save(eventEntity);
    }

    @Test
    void deberiaLanzarExcepcionAlSoftDeletearEventoNoExistente() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.softDeletear(999L));
    }
}
