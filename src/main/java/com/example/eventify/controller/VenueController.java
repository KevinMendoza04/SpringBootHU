package com.example.eventify.controller;

import com.example.eventify.model.Venue;
import com.example.eventify.service.VenueService;
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

@Tag(name = "Lugares", description = "Operaciones para consultar y registrar lugares")
@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping
    @Operation(summary = "Listar lugares")
    public List<Venue> listar() {
        return venueService.listar();
    }

    @PostMapping
    @Operation(summary = "Crear lugar")
    public ResponseEntity<Venue> crear(@RequestBody Venue venue) {
        Venue venueCreado = venueService.crear(venue);
        return ResponseEntity.status(HttpStatus.CREATED).body(venueCreado);
    }
}