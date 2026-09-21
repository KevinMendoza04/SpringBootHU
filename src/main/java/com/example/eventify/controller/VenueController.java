package com.example.eventify.controller;

import com.example.eventify.model.Venue;
import com.example.eventify.service.VenueService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    public ResponseEntity<Venue> crear(@RequestBody Venue venue) {

        Venue creado = venueService.crear(venue);

        return ResponseEntity
                .created(URI.create("/api/venues/" + creado.getId()))
                .body(creado);
    }

    @GetMapping
    public ResponseEntity<Page<Venue>> listar(Pageable pageable) {
        return ResponseEntity.ok(venueService.listar(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venue> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(venueService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Venue> actualizar(
            @PathVariable Long id,
            @RequestBody Venue venue) {

        return ResponseEntity.ok(venueService.actualizar(id, venue));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        venueService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}