package com.example.eventify.controller;

import com.example.eventify.dto.EventCreateDTO;
import com.example.eventify.dto.EventResponseDTO;
import com.example.eventify.dto.EventSummaryDTO;
import com.example.eventify.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * EventController - REST API for event management with advanced search and filtering.
 * 
 * Features:
 * - Paginated event listings using Slice (no total count required)
 * - DTO-based API: EventCreateDTO for input, EventResponseDTO for output
 * - EventSummaryDTO for efficient catalog display (maintains week 4 optimization)
 * - Advanced search filters: city, category, date range, venue capacity
 * - Soft delete operations with logical deletion preservation
 * - Comprehensive Swagger/OpenAPI documentation with RFC 7807 error handling
 */
@Tag(
    name = "Events",
    description = "Advanced event management with filtering, search, and DTO-based API design"
)
@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * List all active events with pagination (Slice-based for performance).
     * Returns full Event entities with lazy-loaded relationships (venue, categories).
     * 
     * @param page Page number (0-indexed)
     * @param size Page size (default: 20)
     * @return Paginated slice of Event entities
     */
    @GetMapping
    @Operation(
        summary = "List events with pagination",
        description = "Retrieves active events ordered by date descending. Uses Slice for optimized queries on massive catalogs without counting total records."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Slice of events retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public ResponseEntity<Slice<?>> listar(
        @Parameter(description = "Page number (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(eventService.listarEventos(pageable));
    }

    /**
     * List event summaries (lightweight projections) for efficient catalog display.
     * Recommended for UI listing pages with many records.
     * MAINTAINS the Record EventSummaryDTO strategy from week 4 for optimization.
     * 
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated slice of EventSummaryDTO objects
     */
    @GetMapping("/summaries")
    @Operation(
        summary = "List event summaries",
        description = "Retrieves lightweight Event projections (EventSummaryDTO) for massive catalogs. Denormalizes venue and category information without loading full entities."
    )
    @ApiResponse(responseCode = "200", description = "Slice of event summaries retrieved")
    public ResponseEntity<Slice<EventSummaryDTO>> listarResumenes(
        @Parameter(description = "Page number (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(eventService.listarEventosSummary(pageable));
    }

    /**
     * Search events by city with case-insensitive partial matching.
     * Ideal for geographic filtering and regional event browsing.
     * 
     * @param ciudad City name (case-insensitive, partial matching supported)
     * @param page Page number
     * @param size Page size
     * @return Filtered event summaries
     */
    @GetMapping("/search/city")
    @Operation(
        summary = "Search events by city",
        description = "Filters events by city with case-insensitive and partial matching. Uses optimized projections (EventSummaryDTO)."
    )
    @ApiResponse(responseCode = "200", description = "Events from the city retrieved")
    public ResponseEntity<Slice<EventSummaryDTO>> buscarPorCiudad(
        @Parameter(description = "City name (partial and case-insensitive search)", example = "bogotá")
        @RequestParam String ciudad,
        @Parameter(description = "Page number", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(eventService.buscarPorCiudad(ciudad, pageable));
    }

    /**
     * Search events by category with case-insensitive partial matching.
     * Filters events linked to categories matching the search term.
     * 
     * @param categoria Category name (case-insensitive, partial matching)
     * @param page Page number
     * @param size Page size
     * @return Filtered event summaries
     */
    @GetMapping("/search/category")
    @Operation(
        summary = "Search events by category",
        description = "Filters events by associated category with case-insensitive and partial matching."
    )
    @ApiResponse(responseCode = "200", description = "Events from the category retrieved")
    public ResponseEntity<Slice<EventSummaryDTO>> buscarPorCategoria(
        @Parameter(description = "Category name (partial and case-insensitive search)", example = "concerts")
        @RequestParam String categoria,
        @Parameter(description = "Page number", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(eventService.buscarPorCategoria(categoria, pageable));
    }

    /**
     * Advanced search combining city and category filters.
     * Returns events that match both criteria (AND logic).
     * 
     * @param ciudad City name
     * @param categoria Category name
     * @param page Page number
     * @param size Page size
     * @return Filtered event summaries
     */
    @GetMapping("/search/city-category")
    @Operation(
        summary = "Search events by city and category",
        description = "Filters events combining city and category criteria (AND logic). Single query optimization without N+1 problems."
    )
    @ApiResponse(responseCode = "200", description = "Events filtered by both criteria")
    public ResponseEntity<Slice<EventSummaryDTO>> buscarPorCiudadYCategoria(
        @Parameter(description = "City name", example = "bogotá")
        @RequestParam String ciudad,
        @Parameter(description = "Category name", example = "concerts")
        @RequestParam String categoria,
        @Parameter(description = "Page number", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(eventService.buscarPorCiudadYCategoria(ciudad, categoria, pageable));
    }

    /**
     * Create a new event with mandatory venue and optional categories.
     * Accepts EventCreateDTO with comprehensive validation.
     * Returns EventResponseDTO with denormalized venue and category information.
     * 
     * @param eventCreateDTO Event creation request DTO with validation
     * @return EventResponseDTO with created event details
     */
    @PostMapping
    @Operation(
        summary = "Create event",
        description = "Creates a new event with mandatory venue. Accepts EventCreateDTO with validation. Returns EventResponseDTO with safe denormalization."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Event created successfully",
            content = @Content(schema = @Schema(implementation = EventResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - see error details for field-level validation messages"
        ),
        @ApiResponse(responseCode = "404", description = "Venue or Category not found")
    })
    public ResponseEntity<EventResponseDTO> crear(
        @Valid @RequestBody EventCreateDTO eventCreateDTO
    ) {
        EventResponseDTO createdEvent = eventService.crear(eventCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    /**
     * Retrieve detailed information about a specific event.
     * Returns EventResponseDTO with denormalized information.
     * 
     * @param id Event ID
     * @return EventResponseDTO with event details
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get event details",
        description = "Retrieves complete event information including venue and categories. Returns EventResponseDTO with denormalized data."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Event found",
            content = @Content(schema = @Schema(implementation = EventResponseDTO.class))
        ),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<EventResponseDTO> obtenerPorId(
        @Parameter(description = "Event ID", example = "1")
        @PathVariable Long id
    ) {
        return eventService.obtenerPorId(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Soft delete an event (logical deletion).
     * Event remains in database but marked as inactive (is_active=false).
     * Automatically excluded from all queries due to @SQLRestriction.
     * 
     * @param id Event ID to soft delete
     * @return Success response
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete event (soft delete)",
        description = "Performs a logical deletion of the event. Record persists in DB but marked inactive. Automatically excluded from all queries via @SQLRestriction."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Event soft deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<Void> eliminarSoft(
        @Parameter(description = "Event ID to delete", example = "1")
        @PathVariable Long id
    ) {
        eventService.softDeletear(id);
        return ResponseEntity.noContent().build();
    }
}
