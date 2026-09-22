package com.example.eventify.controller;

import com.example.eventify.model.Event;
import com.example.eventify.model.Venue;
import com.example.eventify.service.EventService;
import com.example.eventify.service.VenueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private VenueService venueService;

    // ── Escenario 1: Panel principal con datos (camino feliz) ─────────────────

    @Test
    void panelDeberiaRetornar200ConListasEnModelo() throws Exception {
        List<Event> eventos = List.of(
                new Event(1L, "Concierto de rock", LocalDate.of(2026, 10, 20), "Música en vivo")
        );
        List<Venue> lugares = List.of(
                new Venue(1L, "Teatro Central", "Calle 10 #25-30", 800)
        );

        when(eventService.listar()).thenReturn(eventos);
        when(venueService.listar()).thenReturn(lugares);

        mockMvc.perform(get("/admin/"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/index"))
                .andExpect(model().attributeExists("eventos"))
                .andExpect(model().attributeExists("lugares"))
                .andExpect(model().attribute("eventos", hasSize(1)))
                .andExpect(model().attribute("lugares", hasSize(1)));
    }

    // ── Escenario 2: Panel con catálogo vacío (caso de borde) ─────────────────

    @Test
    void panelConListasVaciasDeberiaRetornar200() throws Exception {
        when(eventService.listar()).thenReturn(Collections.emptyList());
        when(venueService.listar()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/index"))
                .andExpect(model().attribute("eventos", hasSize(0)))
                .andExpect(model().attribute("lugares", hasSize(0)));
    }

    // ── Formulario nuevo evento ───────────────────────────────────────────────

    @Test
    void formularioNuevoEventoDeberiaRetornar200ConObjetoVacio() throws Exception {
        mockMvc.perform(get("/admin/eventos/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/nuevo-evento"))
                .andExpect(model().attributeExists("evento"))
                .andExpect(model().attribute("evento", instanceOf(Event.class)));
    }

    // ── Escenario 3: POST evento → guardar y redirigir (PRG) ─────────────────

    @Test
    void guardarEventoDeberiaRedirigirAlPanel() throws Exception {
        Event eventoGuardado = new Event(1L, "Concierto de rock",
                LocalDate.of(2026, 10, 20), "Música en vivo");
        when(eventService.crear(any(Event.class))).thenReturn(eventoGuardado);

        mockMvc.perform(post("/admin/eventos")
                        .param("nombre", "Concierto de rock")
                        .param("fecha", "2026-10-20")
                        .param("descripcion", "Música en vivo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/"));

        verify(eventService).crear(any(Event.class));
    }

    // ── Formulario nuevo lugar ────────────────────────────────────────────────

    @Test
    void formularioNuevoLugarDeberiaRetornar200ConObjetoVacio() throws Exception {
        mockMvc.perform(get("/admin/lugares/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/nuevo-lugar"))
                .andExpect(model().attributeExists("venue"))
                .andExpect(model().attribute("venue", instanceOf(Venue.class)));
    }

    // ── POST lugar → guardar y redirigir (PRG) ────────────────────────────────

    @Test
    void guardarLugarDeberiaRedirigirAlPanel() throws Exception {
        Venue lugarGuardado = new Venue(1L, "Teatro Central", "Calle 10 #25-30", 800);
        when(venueService.crear(any(Venue.class))).thenReturn(lugarGuardado);

        mockMvc.perform(post("/admin/lugares")
                        .param("nombre", "Teatro Central")
                        .param("direccion", "Calle 10 #25-30")
                        .param("capacidad", "800"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/"));

        verify(venueService).crear(any(Venue.class));
    }
}
