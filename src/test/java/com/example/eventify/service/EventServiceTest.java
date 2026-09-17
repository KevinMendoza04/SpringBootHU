package com.example.eventify.service;

import com.example.eventify.exception.InvalidEventException;
import com.example.eventify.model.Event;
import com.example.eventify.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void deberiaGuardarEventoValido() {
        Event evento = new Event(null, "Concierto", LocalDate.of(2026, 10, 20), "Música");
        Event eventoGuardado = new Event(1L, "Concierto", LocalDate.of(2026, 10, 20), "Música");

        when(eventRepository.guardar(evento)).thenReturn(eventoGuardado);

        Event resultado = eventService.crear(evento);

        assertEquals(1L, resultado.getId());
        verify(eventRepository).guardar(evento);
    }

    @Test
    void deberiaRechazarEventoSinNombre() {
        Event evento = new Event(null, "   ", LocalDate.of(2026, 10, 20), "Música");

        assertThrows(InvalidEventException.class, () -> eventService.crear(evento));

        verifyNoInteractions(eventRepository);
    }
}