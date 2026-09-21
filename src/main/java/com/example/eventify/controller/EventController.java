package com.example.eventify.controller;

import com.example.eventify.model.Event;
import com.example.eventify.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<Event> crear(@RequestBody Event event) {

        Event creado = eventService.crear(event);

        return ResponseEntity
                .created(URI.create("/api/events/" + creado.getId()))
                .body(creado);
    }

    @GetMapping
    public ResponseEntity<Page<Event>> listar(Pageable pageable) {
        return ResponseEntity.ok(eventService.listar(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> actualizar(
            @PathVariable Long id,
            @RequestBody Event event) {

        return ResponseEntity.ok(eventService.actualizar(id, event));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        eventService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}