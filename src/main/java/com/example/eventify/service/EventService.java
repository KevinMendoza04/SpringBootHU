package com.example.eventify.service;

import com.example.eventify.dto.EventSummaryDTO;
import com.example.eventify.exception.InvalidEventException;
import com.example.eventify.model.Event;
import com.example.eventify.model.Category;
import com.example.eventify.model.Venue;
import com.example.eventify.repository.EventRepository;
import com.example.eventify.repository.CategoryRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

/**
 * EventService - Orchestrates business logic for event management with optimized data access patterns.
 * 
 * Key Features:
 * - Event creation with mandatory Venue assignment and Category linking
 * - Soft delete mechanism with logical deletion and restoration
 * - Performance-optimized query methods using EventSummaryDTO projections and Slice pagination
 * - N+1 query resolution through @EntityGraph and JPQL constructor expressions
 * - Comprehensive logging for audit trail and observability
 */
@Service
@Transactional
public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    public EventService(EventRepository eventRepository, CategoryRepository categoryRepository) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Retrieve all active events with full entity graph (venue + categories loaded).
     * Uses Slice for pagination without counting total records.
     * 
     * @param pageable Pagination parameters
     * @return Slice of Event entities ordered by date descending
     */
    public Slice<Event> listarEventos(Pageable pageable) {
        logger.debug("Fetching events with pagination: {}", pageable);
        return eventRepository.findAllByOrderByFechaDesc(pageable);
    }

    /**
     * Retrieve event summaries (denormalized DTOs) for massive catalog display.
     * Optimized for UI presentation without loading full entity graphs.
     * 
     * @param pageable Pagination parameters (Slice-based)
     * @return Slice of EventSummaryDTO projections
     */
    public Slice<EventSummaryDTO> listarEventosSummary(Pageable pageable) {
        logger.debug("Fetching event summaries with pagination: {}", pageable);
        return eventRepository.findAllEventSummaries(pageable);
    }

    /**
     * Search event summaries by city with case-insensitive partial matching.
     * 
     * @param ciudad City name (supports partial matching)
     * @param pageable Pagination parameters
     * @return Slice of filtered EventSummaryDTO objects
     */
    public Slice<EventSummaryDTO> buscarPorCiudad(String ciudad, Pageable pageable) {
        logger.debug("Searching events by city: {} with pagination: {}", ciudad, pageable);
        return eventRepository.findEventSummariesByCity(ciudad, pageable);
    }

    /**
     * Search event summaries by category with case-insensitive partial matching.
     * 
     * @param categoryName Category name (supports partial matching)
     * @param pageable Pagination parameters
     * @return Slice of filtered EventSummaryDTO objects
     */
    public Slice<EventSummaryDTO> buscarPorCategoria(String categoryName, Pageable pageable) {
        logger.debug("Searching events by category: {} with pagination: {}", categoryName, pageable);
        return eventRepository.findEventSummariesByCategory(categoryName, pageable);
    }

    /**
     * Search event summaries with combined city and category filters.
     * Single optimized query resolves both filter dimensions.
     * 
     * @param ciudad City name
     * @param categoryName Category name
     * @param pageable Pagination parameters
     * @return Slice of filtered EventSummaryDTO objects
     */
    public Slice<EventSummaryDTO> buscarPorCiudadYCategoria(String ciudad, String categoryName, Pageable pageable) {
        logger.debug("Searching events by city: {} and category: {} with pagination: {}", ciudad, categoryName, pageable);
        return eventRepository.findEventSummariesByCityAndCategory(ciudad, categoryName, pageable);
    }

    /**
     * Create a new event with mandatory venue and optional categories.
     * Validates all business rules before persistence.
     * 
     * @param event Event entity with venue (required) and categories (optional)
     * @return Persisted Event with assigned ID
     * @throws InvalidEventException if validation fails
     */
    public Event crear(Event event) {
        logger.info("Creating new event: {}", event.getNombre());

        if (event == null || event.getNombre() == null || event.getNombre().isBlank()) {
            throw new InvalidEventException("El nombre del evento es obligatorio");
        }

        if (event.getFecha() == null) {
            throw new InvalidEventException("La fecha del evento es obligatoria");
        }

        if (event.getDescripcion() == null || event.getDescripcion().isBlank()) {
            throw new InvalidEventException("La descripción del evento es obligatoria");
        }

        if (event.getVenue() == null || event.getVenue().getId() == null) {
            throw new InvalidEventException("El lugar (venue) es obligatorio para crear un evento");
        }

        event.setIsActive(true);

        Event savedEvent = eventRepository.save(event);
        logger.info("Event created successfully with ID: {}", savedEvent.getId());

        return savedEvent;
    }

    /**
     * Find event by ID with full entity graph loaded.
     * 
     * @param id Event ID
     * @return Optional containing the Event if found
     */
    public Optional<Event> obtenerPorId(Long id) {
        logger.debug("Fetching event by ID: {}", id);
        return eventRepository.findById(id);
    }

    /**
     * Soft delete an event (logical deletion).
     * Event remains in database but marked as inactive.
     * Automatically filtered from all queries via @SQLRestriction.
     * 
     * @param id Event ID to soft delete
     * @throws InvalidEventException if event not found
     */
    public void softDeletear(Long id) {
        logger.info("Soft deleting event with ID: {}", id);

        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new InvalidEventException("Evento no encontrado con ID: " + id));

        event.softDelete();
        eventRepository.save(event);

        logger.info("Event soft deleted successfully: {}", id);
    }

    /**
     * Restore a soft-deleted event back to active status.
     * 
     * @param id Event ID to restore
     * @throws InvalidEventException if event not found
     */
    public void restaurar(Long id) {
        logger.info("Restoring soft-deleted event with ID: {}", id);

        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new InvalidEventException("Evento no encontrado con ID: " + id));

        event.restore();
        eventRepository.save(event);

        logger.info("Event restored successfully: {}", id);
    }

    /**
     * Add a category to an event (update many-to-many relationship).
     * Categories are created or fetched by name, then linked to event.
     * 
     * @param eventId Event ID
     * @param categoryNames Set of category names to associate
     * @throws InvalidEventException if event not found
     */
    public void agregarCategorias(Long eventId, Set<String> categoryNames) {
        logger.debug("Adding categories to event {}: {}", eventId, categoryNames);

        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new InvalidEventException("Evento no encontrado con ID: " + eventId));

        for (String categoryName : categoryNames) {
            Category category = categoryRepository.findByNombreIgnoreCase(categoryName)
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setNombre(categoryName);
                    return categoryRepository.save(newCategory);
                });

            event.getCategories().add(category);
        }

        eventRepository.save(event);
        logger.debug("Categories added successfully to event: {}", eventId);
    }
}