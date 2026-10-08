package com.example.eventify.service;

import com.example.eventify.dto.VenueCreateDTO;
import com.example.eventify.dto.VenueResponseDTO;
import com.example.eventify.exception.DuplicateResourceException;
import com.example.eventify.exception.ResourceNotFoundException;
import com.example.eventify.mapper.VenueMapper;
import com.example.eventify.model.Venue;
import com.example.eventify.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * VenueServiceTest - Unit tests for VenueService with DTO architecture
 */
@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private VenueMapper venueMapper;

    @InjectMocks
    private VenueService venueService;

    private VenueCreateDTO validVenueDTO;
    private Venue savedVenue;
    private VenueResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        validVenueDTO = new VenueCreateDTO();
        validVenueDTO.setNombre("Teatro Central");
        validVenueDTO.setDireccion("Calle Principal 123");
        validVenueDTO.setCapacidad(800);
        validVenueDTO.setCiudad("Bogotá");

        savedVenue = new Venue();
        savedVenue.setId(1L);
        savedVenue.setNombre("Teatro Central");
        savedVenue.setDireccion("Calle Principal 123");
        savedVenue.setCapacidad(800);
        savedVenue.setCiudad("Bogotá");

        responseDTO = new VenueResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setNombre("Teatro Central");
        responseDTO.setDireccion("Calle Principal 123");
        responseDTO.setCapacidad(800);
        responseDTO.setCiudad("Bogotá");
    }

    @Test
    void deberiaGuardarVenueValido() {
        when(venueRepository.findByNombreIgnoreCase("Teatro Central")).thenReturn(Optional.empty());
        when(venueMapper.toEntity(validVenueDTO)).thenReturn(savedVenue);
        when(venueRepository.save(any(Venue.class))).thenReturn(savedVenue);
        when(venueMapper.toResponseDTO(savedVenue)).thenReturn(responseDTO);

        VenueResponseDTO resultado = venueService.crear(validVenueDTO);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Teatro Central", resultado.getNombre());
        verify(venueRepository).save(any(Venue.class));
    }

    @Test
    void deberiaRechazarVenueDuplicado() {
        when(venueRepository.findByNombreIgnoreCase("Teatro Central")).thenReturn(Optional.of(savedVenue));

        assertThrows(DuplicateResourceException.class, () -> venueService.crear(validVenueDTO));
        verify(venueRepository, never()).save(any());
    }

    @Test
    void deberiaObtenerVenuePorId() {
        when(venueRepository.findById(1L)).thenReturn(Optional.of(savedVenue));
        when(venueMapper.toResponseDTO(savedVenue)).thenReturn(responseDTO);

        var resultado = venueService.obtenerPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Teatro Central", resultado.get().getNombre());
    }

    @Test
    void deberiaLanzarExcepcionAlObtenerVenueNoExistente() {
        when(venueRepository.findById(999L)).thenReturn(Optional.empty());

        var resultado = venueService.obtenerPorId(999L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deberiaEliminarVenueExistente() {
        when(venueRepository.findById(1L)).thenReturn(Optional.of(savedVenue));

        venueService.eliminar(1L);

        verify(venueRepository).delete(savedVenue);
    }

    @Test
    void deberiaLanzarExcepcionAlEliminarVenueNoExistente() {
        when(venueRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> venueService.eliminar(999L));
    }
}
