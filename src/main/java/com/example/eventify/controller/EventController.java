package com.example.eventify.controller;

import com.example.eventify.dto.EventSummaryDTO;
import com.example.eventify.model.Event;
import com.example.eventify.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * EventController - REST API for event management with advanced search and filtering.
 * 
 * Features:
 * - Paginated event listings using Slice (no total count required)
 * - Advanced search filters: city, category, date range, venue capacity
 * - EventSummaryDTO projections for efficient catalog display
 * - Soft delete operations with logical deletion preservation
 * - Comprehensive Swagger/OpenAPI documentation
 */
@Tag(name = "Eventos", description = "Operaciones avanzadas de consulta, creación y gestión de eventos con filtrado inteligente y soft delete")
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
        summary = "Listar eventos con paginación",
        description = "Recupera eventos activos ordenados por fecha descendente. Utiliza Slice para optimizar queries en catálogos masivos sin contar registros totales."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Slice de eventos recuperado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos")
    })
    public ResponseEntity<Slice<Event>> listar(
        @Parameter(description = "Número de página (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Tamaño de página", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(eventService.listarEventos(pageable));
    }

    /**
     * List event summaries (lightweight projections) for efficient catalog display.
     * Recommended for UI listing pages with many records.
     * 
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated slice of EventSummaryDTO objects
     */
    @GetMapping("/summaries")
    @Operation(
        summary = "Listar resúmenes de eventos",
        description = "Recupera proyecciones ligeras de eventos (EventSummaryDTO) para catálogos masivos. Denormaliza información del venue y categorías sin cargar entidades completas."
    )
    @ApiResponse(responseCode = "200", description = "Slice de resúmenes de eventos recuperado")
    public ResponseEntity<Slice<EventSummaryDTO>> listarResumenes(
        @Parameter(description = "Número de página (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Tamaño de página", example = "20")
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
    @GetMapping("/buscar/ciudad")
    @Operation(
        summary = "Buscar eventos por ciudad",
        description = "Filtra eventos por ciudad con búsqueda insensible a mayúsculas y parcial. Utiliza proyecciones optimizadas (EventSummaryDTO)."
    )
    @ApiResponse(responseCode = "200", description = "Eventos de la ciudad recuperados")
    public ResponseEntity<Slice<EventSummaryDTO>> buscarPorCiudad(
        @Parameter(description = "Nombre de la ciudad (búsqueda parcial e insensible a mayúsculas)", example = "bogotá")
        @RequestParam String ciudad,
        @Parameter(description = "Número de página", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Tamaño de página", example = "20")
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
    @GetMapping("/buscar/categoria")
    @Operation(
        summary = "Buscar eventos por categoría",
        description = "Filtra eventos por categoría asociada con búsqueda insensible a mayúsculas y parcial."
    )
    @ApiResponse(responseCode = "200", description = "Eventos de la categoría recuperados")
    public ResponseEntity<Slice<EventSummaryDTO>> buscarPorCategoria(
        @Parameter(description = "Nombre de la categoría (búsqueda parcial e insensible a mayúsculas)", example = "conciertos")
        @RequestParam String categoria,
        @Parameter(description = "Número de página", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Tamaño de página", example = "20")
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
    @GetMapping("/buscar/ciudad-categoria")
    @Operation(
        summary = "Buscar eventos por ciudad y categoría",
        description = "Filtra eventos combinando criterios de ciudad y categoría (lógica AND). Single query optimization sin problemas N+1."
    )
    @ApiResponse(responseCode = "200", description = "Eventos filtrados por ambos criterios")
    public ResponseEntity<Slice<EventSummaryDTO>> buscarPorCiudadYCategoria(
        @Parameter(description = "Nombre de la ciudad", example = "bogotá")
        @RequestParam String ciudad,
        @Parameter(description = "Nombre de la categoría", example = "conciertos")
        @RequestParam String categoria,
        @Parameter(description = "Número de página", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Tamaño de página", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(eventService.buscarPorCiudadYCategoria(ciudad, categoria, pageable));
    }

    /**
     * Create a new event with mandatory venue and optional categories.
     * Event is created in active state (is_active=true) by default.
     * 
     * @param event Event object with venue (required)
     * @return Created Event with assigned ID
     */
    @PostMapping
    @Operation(
        summary = "Crear evento",
        description = "Crea un nuevo evento con venue obligatorio. El evento se crea en estado activo. Las categorías se pueden asignar posteriormente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Evento creado exitosamente", content = @Content(schema = @Schema(implementation = Event.class))),
        @ApiResponse(responseCode = "400", description = "Datos de evento inválidos o venue no especificado")
    })
    public ResponseEntity<Event> crear(@RequestBody Event event) {
        Event eventoCreado = eventService.crear(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventoCreado);
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
        summary = "Eliminar evento (soft delete)",
        description = "Realiza un borrado lógico del evento. El registro persiste en BD pero marcado como inactivo (is_active=false). Automáticamente excluido de todas las consultas por @SQLRestriction."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Evento eliminado (soft delete) exitosamente"),
        @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    public ResponseEntity<Void> eliminarSoft(
        @Parameter(description = "ID del evento a eliminar", example = "1")
        @PathVariable Long id
    ) {
        eventService.softDeletear(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieve detailed information about a specific event.
     * 
     * @param id Event ID
     * @return Event details with venue and categories loaded
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener detalles de un evento",
        description = "Recupera la información completa de un evento incluyendo venue y categorías asociadas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Evento encontrado"),
        @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    public ResponseEntity<Event> obtenerPorId(
        @Parameter(description = "ID del evento", example = "1")
        @PathVariable Long id
    ) {
        return eventService.obtenerPorId(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}