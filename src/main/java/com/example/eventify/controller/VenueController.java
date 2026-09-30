package com.example.eventify.controller;

import com.example.eventify.model.Venue;
import com.example.eventify.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * VenueController - REST API for venue/location management with search capabilities.
 * 
 * Features:
 * - CRUD operations for venue management
 * - Search by city and name with case-insensitive partial matching
 * - Comprehensive Swagger/OpenAPI documentation
 */
@Tag(name = "Lugares", description = "Operaciones para consultar, crear y gestionar lugares (venues) físicos para eventos")
@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    /**
     * List all venues.
     * 
     * @return List of all Venue entities
     */
    @GetMapping
    @Operation(
        summary = "Listar todos los lugares",
        description = "Recupera el listado completo de lugares registrados en el sistema."
    )
    @ApiResponse(responseCode = "200", description = "Listado de lugares recuperado")
    public ResponseEntity<List<Venue>> listar() {
        return ResponseEntity.ok(venueService.listar());
    }

    /**
     * Search venues by city with case-insensitive partial matching.
     * 
     * @param ciudad City name
     * @return Venues in specified city
     */
    @GetMapping("/buscar/ciudad")
    @Operation(
        summary = "Buscar lugares por ciudad",
        description = "Filtra lugares por ciudad con búsqueda insensible a mayúsculas y parcial."
    )
    @ApiResponse(responseCode = "200", description = "Lugares de la ciudad recuperados")
    public ResponseEntity<List<Venue>> buscarPorCiudad(
        @Parameter(description = "Nombre de la ciudad (búsqueda parcial e insensible a mayúsculas)", example = "bogotá")
        @RequestParam String ciudad
    ) {
        return ResponseEntity.ok(venueService.buscarPorCiudad(ciudad));
    }

    /**
     * Search venues by name with case-insensitive partial matching.
     * 
     * @param nombre Venue name
     * @return Venues matching the name
     */
    @GetMapping("/buscar/nombre")
    @Operation(
        summary = "Buscar lugares por nombre",
        description = "Filtra lugares por nombre con búsqueda insensible a mayúsculas y parcial."
    )
    @ApiResponse(responseCode = "200", description = "Lugares encontrados")
    public ResponseEntity<List<Venue>> buscarPorNombre(
        @Parameter(description = "Nombre del lugar (búsqueda parcial)", example = "auditorio")
        @RequestParam String nombre
    ) {
        return ResponseEntity.ok(venueService.buscarPorNombre(nombre));
    }

    /**
     * Get venue details by ID.
     * 
     * @param id Venue ID
     * @return Venue details
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener detalles de un lugar",
        description = "Recupera la información completa de un lugar específico."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lugar encontrado"),
        @ApiResponse(responseCode = "404", description = "Lugar no encontrado")
    })
    public ResponseEntity<Venue> obtenerPorId(
        @Parameter(description = "ID del lugar", example = "1")
        @PathVariable Long id
    ) {
        return venueService.obtenerPorId(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create a new venue.
     * 
     * @param venue Venue object with all required fields
     * @return Created Venue with assigned ID
     */
    @PostMapping
    @Operation(
        summary = "Crear lugar",
        description = "Crea un nuevo lugar con capacidad, dirección y ciudad obligatorios."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Lugar creado exitosamente", content = @Content(schema = @Schema(implementation = Venue.class))),
        @ApiResponse(responseCode = "400", description = "Datos de lugar inválidos")
    })
    public ResponseEntity<Venue> crear(@RequestBody Venue venue) {
        Venue venueCreado = venueService.crear(venue);
        return ResponseEntity.status(HttpStatus.CREATED).body(venueCreado);
    }

    /**
     * Update an existing venue.
     * 
     * @param id Venue ID to update
     * @param venue Updated Venue data
     * @return Updated Venue
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar lugar",
        description = "Actualiza los datos de un lugar existente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lugar actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Lugar no encontrado")
    })
    public ResponseEntity<Venue> actualizar(
        @Parameter(description = "ID del lugar", example = "1")
        @PathVariable Long id,
        @RequestBody Venue venue
    ) {
        try {
            Venue updated = venueService.actualizar(id, venue);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a venue by ID.
     * 
     * @param id Venue ID to delete
     * @return Success response
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar lugar",
        description = "Elimina un lugar del sistema. Esta operación puede fallar si hay eventos vinculados al lugar."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Lugar eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Lugar no encontrado")
    })
    public ResponseEntity<Void> eliminar(
        @Parameter(description = "ID del lugar", example = "1")
        @PathVariable Long id
    ) {
        try {
            venueService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}