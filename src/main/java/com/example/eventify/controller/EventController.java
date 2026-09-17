package com.example.eventify.controller;

import com.example.eventify.model.Event;
import com.example.eventify.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Eventos", description = "Operaciones para consultar y registrar eventos")
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @Operation(summary = "Listar eventos")
    public List<Event> listar() {
        return eventService.listar();
    }

    @PostMapping
    @Operation(summary = "Crear evento")
    public ResponseEntity<Event> crear(@RequestBody Event event) {
        Event eventoCreado = eventService.crear(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventoCreado);
    }
}