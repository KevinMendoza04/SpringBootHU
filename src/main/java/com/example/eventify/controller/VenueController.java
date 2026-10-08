package com.example.eventify.controller;

import com.example.eventify.dto.VenueCreateDTO;
import com.example.eventify.dto.VenueResponseDTO;
import com.example.eventify.model.Venue;
import com.example.eventify.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * VenueController - REST API for venue/location management with search capabilities.
 * 
 * Features:
 * - DTO-based CRUD operations (VenueCreateDTO input, VenueResponseDTO output)
 * - Search by city and name with case-insensitive partial matching
 * - Comprehensive validation with Jakarta Bean Validation
 * - RFC 7807 error handling with ProblemDetail responses
 * - Complete Swagger/OpenAPI documentation
 */
@Tag(
    name = "Venues",
    description = "Operations for consulting, creating, and managing physical venues for events"
)
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
        summary = "List all venues",
        description = "Retrieves the complete list of registered venues in the system."
    )
    @ApiResponse(responseCode = "200", description = "List of venues retrieved")
    public ResponseEntity<List<Venue>> listar() {
        return ResponseEntity.ok(venueService.listar());
    }

    /**
     * Search venues by city with case-insensitive partial matching.
     * 
     * @param ciudad City name
     * @return Venues in specified city
     */
    @GetMapping("/search/city")
    @Operation(
        summary = "Search venues by city",
        description = "Filters venues by city with case-insensitive and partial matching."
    )
    @ApiResponse(responseCode = "200", description = "Venues from the city retrieved")
    public ResponseEntity<List<Venue>> buscarPorCiudad(
        @Parameter(description = "City name (partial and case-insensitive search)", example = "bogotá")
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
    @GetMapping("/search/name")
    @Operation(
        summary = "Search venues by name",
        description = "Filters venues by name with case-insensitive and partial matching."
    )
    @ApiResponse(responseCode = "200", description = "Venues found")
    public ResponseEntity<List<Venue>> buscarPorNombre(
        @Parameter(description = "Venue name (partial search)", example = "auditorio")
        @RequestParam String nombre
    ) {
        return ResponseEntity.ok(venueService.buscarPorNombre(nombre));
    }

    /**
     * Get venue details by ID.
     * Returns VenueResponseDTO with safe information exposure.
     * 
     * @param id Venue ID
     * @return VenueResponseDTO with venue details
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get venue details",
        description = "Retrieves complete information about a specific venue."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Venue found",
            content = @Content(schema = @Schema(implementation = VenueResponseDTO.class))
        ),
        @ApiResponse(responseCode = "404", description = "Venue not found")
    })
    public ResponseEntity<VenueResponseDTO> obtenerPorId(
        @Parameter(description = "Venue ID", example = "1")
        @PathVariable Long id
    ) {
        return venueService.obtenerPorId(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create a new venue.
     * Accepts VenueCreateDTO with comprehensive validation.
     * 
     * @param venueCreateDTO Venue creation request DTO
     * @return VenueResponseDTO with created venue details
     */
    @PostMapping
    @Operation(
        summary = "Create venue",
        description = "Creates a new venue with capacity, address, and city as mandatory fields. Accepts VenueCreateDTO with validation."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Venue created successfully",
            content = @Content(schema = @Schema(implementation = VenueResponseDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Validation error - see error details"),
        @ApiResponse(responseCode = "409", description = "Venue with this name already exists")
    })
    public ResponseEntity<VenueResponseDTO> crear(
        @Valid @RequestBody VenueCreateDTO venueCreateDTO
    ) {
        VenueResponseDTO createdVenue = venueService.crear(venueCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVenue);
    }

    /**
     * Update an existing venue.
     * Accepts VenueCreateDTO with updated data and comprehensive validation.
     * 
     * @param id Venue ID to update
     * @param venueCreateDTO Updated venue data
     * @return VenueResponseDTO with updated venue
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update venue",
        description = "Updates the data of an existing venue. Accepts VenueCreateDTO with validation."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Venue updated successfully",
            content = @Content(schema = @Schema(implementation = VenueResponseDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "404", description = "Venue not found"),
        @ApiResponse(responseCode = "409", description = "Venue with this name already exists")
    })
    public ResponseEntity<VenueResponseDTO> actualizar(
        @Parameter(description = "Venue ID", example = "1")
        @PathVariable Long id,
        @Valid @RequestBody VenueCreateDTO venueCreateDTO
    ) {
        VenueResponseDTO updated = venueService.actualizar(id, venueCreateDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a venue by ID.
     * Note: Deletion is restricted by foreign key constraints if events are linked.
     * 
     * @param id Venue ID to delete
     * @return Success response
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete venue",
        description = "Deletes a venue from the system. This operation may fail if there are events linked to the venue."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Venue deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Venue not found"),
        @ApiResponse(responseCode = "400", description = "Venue has linked events")
    })
    public ResponseEntity<Void> eliminar(
        @Parameter(description = "Venue ID", example = "1")
        @PathVariable Long id
    ) {
        venueService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
