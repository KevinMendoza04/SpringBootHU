package com.example.eventify.service;

import com.example.eventify.dto.EventCreateDTO;
import com.example.eventify.dto.EventResponseDTO;
import com.example.eventify.dto.EventSummaryDTO;
import com.example.eventify.exception.BusinessRuleViolationException;
import com.example.eventify.exception.ResourceNotFoundException;
import com.example.eventify.mapper.EventMapper;
import com.example.eventify.model.Category;
import com.example.eventify.model.Event;
import com.example.eventify.model.Venue;
import com.example.eventify.repository.CategoryRepository;
import com.example.eventify.repository.EventRepository;
import com.example.eventify.repository.VenueRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * EventService - Orchestrates business logic for event management with optimized data access patterns.
 * 
 * Key Features:
 * - Event creation with mandatory Venue assignment and Category linking
 * - Soft delete mechanism with logical deletion and restoration
 * - Performance-optimized query methods using EventSummaryDTO projections and Slice pagination
 * - DTO-based API: accepts EventCreateDTO, returns EventResponseDTO
 * - N+1 query resolution through @EntityGraph and JPQL constructor expressions
 * - Comprehensive logging for audit trail and observability
 */
@Service
@Transactional
public class EventService {
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);
    
    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;

    public EventService(
            EventRepository eventRepository,
            VenueRepository venueRepository,
            CategoryRepository categoryRepository,
            EventMapper eventMapper
    ) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.categoryRepository = categoryRepository;
        this.eventMapper = eventMapper;
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
     * KEEPS the Record EventSummaryDTO strategy from week 4 for list optimization.
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
     * Create a new event from EventCreateDTO with mandatory venue and optional categories.
     * Validates all business rules before persistence and returns EventResponseDTO.
     * 
     * @param eventCreateDTO DTO with event creation data
     * @return EventResponseDTO with created event details
     * @throws ResourceNotFoundException if venue not found
     * @throws BusinessRuleViolationException if validation fails
     */
    public EventResponseDTO crear(EventCreateDTO eventCreateDTO) {
        logger.info("Creating new event: {}", eventCreateDTO.getNombre());

        // Convert DTO to entity
        Event event = eventMapper.toEntity(eventCreateDTO);
        event.setIsActive(true);

        // Load and validate venue
        if (eventCreateDTO.getVenueId() == null || eventCreateDTO.getVenueId() <= 0) {
            throw new BusinessRuleViolationException(
                "El lugar (venue) es obligatorio para crear un evento",
                "VENUE_REQUIRED"
            );
        }

        Venue venue = venueRepository.findById(eventCreateDTO.getVenueId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Venue",
                "id",
                eventCreateDTO.getVenueId()
            ));

        event.setVenue(venue);

        // Load and assign categories if provided
        if (eventCreateDTO.getCategoryIds() != null && !eventCreateDTO.getCategoryIds().isEmpty()) {
            Set<Category> categories = eventCreateDTO.getCategoryIds().stream()
                .map(categoryId -> categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId)))
                .collect(Collectors.toSet());
            event.setCategories(categories);
        }

        Event savedEvent = eventRepository.save(event);
        logger.info("Event created successfully with ID: {}", savedEvent.getId());

        return eventMapper.toResponseDTO(savedEvent);
    }

    /**
     * Find event by ID and return EventResponseDTO.
     * 
     * @param id Event ID
     * @return Optional containing EventResponseDTO if found
     */
    public Optional<EventResponseDTO> obtenerPorId(Long id) {
        logger.debug("Fetching event by ID: {}", id);
        return eventRepository.findById(id)
            .map(eventMapper::toResponseDTO);
    }

    /**
     * Soft delete an event (logical deletion).
     * Event remains in database but marked as inactive.
     * Automatically filtered from all queries via @SQLRestriction.
     * 
     * @param id Event ID to soft delete
     * @throws ResourceNotFoundException if event not found
     */
    public void softDeletear(Long id) {
        logger.info("Soft deleting event with ID: {}", id);
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", id));
        event.softDelete();
        eventRepository.save(event);
        logger.info("Event soft deleted successfully: {}", id);
    }

    /**
     * Restore a soft-deleted event back to active status.
     * 
     * @param id Event ID to restore
     * @throws ResourceNotFoundException if event not found
     */
    public void restaurar(Long id) {
        logger.info("Restoring soft-deleted event with ID: {}", id);
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", id));
        event.restore();
        eventRepository.save(event);
        logger.info("Event restored successfully: {}", id);
    }

    /**
     * Add categories to an event (update many-to-many relationship).
     * 
     * @param eventId Event ID
     * @param categoryNames Set of category names to associate
     * @throws ResourceNotFoundException if event not found
     */
    public void agregarCategorias(Long eventId, Set<String> categoryNames) {
        logger.debug("Adding categories to event {}: {}", eventId, categoryNames);
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", eventId));
        
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
