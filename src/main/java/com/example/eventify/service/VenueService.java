package com.example.eventify.service;

import com.example.eventify.dto.VenueCreateDTO;
import com.example.eventify.dto.VenueResponseDTO;
import com.example.eventify.exception.BusinessRuleViolationException;
import com.example.eventify.exception.DuplicateResourceException;
import com.example.eventify.exception.ResourceNotFoundException;
import com.example.eventify.mapper.VenueMapper;
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
 * - DTO-based API: accepts VenueCreateDTO, returns VenueResponseDTO
 * - Search methods for city and name with case-insensitive matching
 * - Transaction management for data consistency
 * - Comprehensive logging for audit trail and observability
 */
@Service
@Transactional
public class VenueService {
    private static final Logger logger = LoggerFactory.getLogger(VenueService.class);
    
    private final VenueRepository venueRepository;
    private final VenueMapper venueMapper;

    public VenueService(VenueRepository venueRepository, VenueMapper venueMapper) {
        this.venueRepository = venueRepository;
        this.venueMapper = venueMapper;
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
     * Create a new venue from VenueCreateDTO with comprehensive validation.
     * All fields are mandatory. Returns VenueResponseDTO.
     * 
     * @param venueCreateDTO DTO with venue creation data
     * @return VenueResponseDTO with created venue details
     * @throws BusinessRuleViolationException if validation fails
     */
    public VenueResponseDTO crear(VenueCreateDTO venueCreateDTO) {
        logger.info("Creating new venue: {}", venueCreateDTO.getNombre());

        // Check for existing venue with same name
        Optional<Venue> existingVenue = venueRepository.findByNombreIgnoreCase(venueCreateDTO.getNombre());
        if (existingVenue.isPresent()) {
            throw new DuplicateResourceException(
                "Venue",
                "nombre",
                venueCreateDTO.getNombre()
            );
        }

        // Convert DTO to entity
        Venue venue = venueMapper.toEntity(venueCreateDTO);

        Venue savedVenue = venueRepository.save(venue);
        logger.info("Venue created successfully with ID: {}", savedVenue.getId());

        return venueMapper.toResponseDTO(savedVenue);
    }

    /**
     * Find venue by ID and return VenueResponseDTO.
     * 
     * @param id Venue ID
     * @return Optional containing VenueResponseDTO if found
     */
    public Optional<VenueResponseDTO> obtenerPorId(Long id) {
        logger.debug("Fetching venue by ID: {}", id);
        return venueRepository.findById(id)
            .map(venueMapper::toResponseDTO);
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
     * Update an existing venue from VenueCreateDTO.
     * 
     * @param id Venue ID to update
     * @param venueCreateDTO Updated Venue data
     * @return Updated VenueResponseDTO
     * @throws ResourceNotFoundException if venue not found
     */
    public VenueResponseDTO actualizar(Long id, VenueCreateDTO venueCreateDTO) {
        logger.info("Updating venue with ID: {}", id);
        
        Venue venue = venueRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", id));

        // Check if new name conflicts with another venue (excluding this one)
        if (!venue.getNombre().equalsIgnoreCase(venueCreateDTO.getNombre())) {
            Optional<Venue> existingVenue = venueRepository.findByNombreIgnoreCase(venueCreateDTO.getNombre());
            if (existingVenue.isPresent()) {
                throw new DuplicateResourceException(
                    "Venue",
                    "nombre",
                    venueCreateDTO.getNombre()
                );
            }
        }

        venue.setNombre(venueCreateDTO.getNombre());
        venue.setDireccion(venueCreateDTO.getDireccion());
        venue.setCapacidad(venueCreateDTO.getCapacidad());
        venue.setCiudad(venueCreateDTO.getCiudad());

        Venue updated = venueRepository.save(venue);
        logger.info("Venue updated successfully: {}", id);

        return venueMapper.toResponseDTO(updated);
    }

    /**
     * Delete a venue by ID.
     * Note: Deletion is restricted by foreign key constraints if events are linked.
     * 
     * @param id Venue ID to delete
     * @throws ResourceNotFoundException if venue not found
     * @throws BusinessRuleViolationException if venue has linked events
     */
    public void eliminar(Long id) {
        logger.info("Deleting venue with ID: {}", id);
        
        Venue venue = venueRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", id));

        try {
            venueRepository.delete(venue);
            logger.info("Venue deleted successfully: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting venue: {}", id, e);
            throw new BusinessRuleViolationException(
                "No se puede eliminar el lugar porque tiene eventos vinculados",
                "VENUE_HAS_EVENTS"
            );
        }
    }
}
