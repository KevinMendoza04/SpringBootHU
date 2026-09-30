package com.example.eventify.service;

import com.example.eventify.exception.InvalidVenueException;
import com.example.eventify.model.Venue;
import com.example.eventify.repository.VenueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * VenueService - Orchestrates business logic for venue/location management.
 * 
 * Key Features:
 * - Comprehensive venue CRUD operations with validation
 * - Search methods for city and name with case-insensitive matching
 * - Transaction management for data consistency
 * - Comprehensive logging for audit trail and observability
 */
@Service
@Transactional
public class VenueService {

    private static final Logger logger = LoggerFactory.getLogger(VenueService.class);

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    /**
     * Retrieve all venues from database.
     * 
     * @return List of all Venue entities
     */
    public List<Venue> listar() {
        logger.debug("Fetching all venues");
        return venueRepository.findAll();
    }

    /**
     * Create a new venue with comprehensive validation.
     * All fields except description are mandatory.
     * 
     * @param venue Venue entity to persist
     * @return Persisted Venue with assigned ID
     * @throws InvalidVenueException if validation fails
     */
    public Venue crear(Venue venue) {
        logger.info("Creating new venue: {}", venue.getNombre());

        if (venue == null || venue.getNombre() == null || venue.getNombre().isBlank()) {
            throw new InvalidVenueException("El nombre del lugar es obligatorio");
        }

        if (venue.getCapacidad() == null || venue.getCapacidad() <= 0) {
            throw new InvalidVenueException("La capacidad debe ser mayor que cero");
        }

        if (venue.getDireccion() == null || venue.getDireccion().isBlank()) {
            throw new InvalidVenueException("La dirección del lugar es obligatoria");
        }

        if (venue.getCiudad() == null || venue.getCiudad().isBlank()) {
            throw new InvalidVenueException("La ciudad del lugar es obligatoria");
        }

        Venue savedVenue = venueRepository.save(venue);
        logger.info("Venue created successfully with ID: {}", savedVenue.getId());

        return savedVenue;
    }

    /**
     * Find venue by ID.
     * 
     * @param id Venue ID
     * @return Optional containing the Venue if found
     */
    public Optional<Venue> obtenerPorId(Long id) {
        logger.debug("Fetching venue by ID: {}", id);
        return venueRepository.findById(id);
    }

    /**
     * Search venues by city with case-insensitive partial matching.
     * 
     * @param ciudad City name (supports partial matching)
     * @return List of Venues in specified city
     */
    public List<Venue> buscarPorCiudad(String ciudad) {
        logger.debug("Searching venues by city: {}", ciudad);
        return venueRepository.findByCiudadIgnoreCaseContaining(ciudad);
    }

    /**
     * Search venues by name with case-insensitive partial matching.
     * 
     * @param nombre Venue name (supports partial matching)
     * @return List of Venues matching the name
     */
    public List<Venue> buscarPorNombre(String nombre) {
        logger.debug("Searching venues by name: {}", nombre);
        return venueRepository.findByNombreIgnoreCaseContaining(nombre);
    }

    /**
     * Update an existing venue.
     * 
     * @param id Venue ID to update
     * @param venueActualizado Updated Venue data
     * @return Updated Venue entity
     * @throws InvalidVenueException if venue not found or validation fails
     */
    public Venue actualizar(Long id, Venue venueActualizado) {
        logger.info("Updating venue with ID: {}", id);

        Venue venue = venueRepository.findById(id)
            .orElseThrow(() -> new InvalidVenueException("Lugar no encontrado con ID: " + id));

        if (venueActualizado.getNombre() != null && !venueActualizado.getNombre().isBlank()) {
            venue.setNombre(venueActualizado.getNombre());
        }

        if (venueActualizado.getDireccion() != null && !venueActualizado.getDireccion().isBlank()) {
            venue.setDireccion(venueActualizado.getDireccion());
        }

        if (venueActualizado.getCapacidad() != null && venueActualizado.getCapacidad() > 0) {
            venue.setCapacidad(venueActualizado.getCapacidad());
        }

        if (venueActualizado.getCiudad() != null && !venueActualizado.getCiudad().isBlank()) {
            venue.setCiudad(venueActualizado.getCiudad());
        }

        Venue updated = venueRepository.save(venue);
        logger.info("Venue updated successfully: {}", id);

        return updated;
    }

    /**
     * Delete a venue by ID.
     * Note: Deletion is restricted by foreign key constraints if events are linked.
     * 
     * @param id Venue ID to delete
     * @throws InvalidVenueException if venue not found
     */
    public void eliminar(Long id) {
        logger.info("Deleting venue with ID: {}", id);

        Venue venue = venueRepository.findById(id)
            .orElseThrow(() -> new InvalidVenueException("Lugar no encontrado con ID: " + id));

        venueRepository.delete(venue);
        logger.info("Venue deleted successfully: {}", id);
    }
}