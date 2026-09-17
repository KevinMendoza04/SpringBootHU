package com.example.eventify.service;

import com.example.eventify.exception.InvalidVenueException;
import com.example.eventify.model.Venue;
import com.example.eventify.repository.VenueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private VenueService venueService;

    @Test
    void deberiaGuardarLugarValido() {
        Venue venue = new Venue(null, "Teatro Central", "Calle 10", 800);
        Venue venueGuardado = new Venue(1L, "Teatro Central", "Calle 10", 800);

        when(venueRepository.guardar(venue)).thenReturn(venueGuardado);

        Venue resultado = venueService.crear(venue);

        assertEquals(1L, resultado.getId());
        verify(venueRepository).guardar(venue);
    }

    @Test
    void deberiaRechazarLugarConCapacidadInvalida() {
        Venue venue = new Venue(null, "Teatro Central", "Calle 10", 0);

        assertThrows(InvalidVenueException.class, () -> venueService.crear(venue));

        verifyNoInteractions(venueRepository);
    }
}