package com.example.eventify.mapper;

import com.example.eventify.dto.VenueCreateDTO;
import com.example.eventify.dto.VenueResponseDTO;
import com.example.eventify.model.Venue;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

/**
 * VenueMapper - Mapper for Venue entity and DTOs.
 * Handles conversion between:
 * - Venue (JPA entity) <-> VenueCreateDTO (input)
 * - Venue (JPA entity) <-> VenueResponseDTO (output)
 */
@Component
@Mapper(componentModel = "spring")
public abstract class VenueMapper {

    /**
     * Convert VenueCreateDTO to Venue entity.
     */
    public Venue toEntity(VenueCreateDTO dto) {
        if (dto == null) {
            return null;
        }

        Venue venue = new Venue();
        venue.setNombre(dto.getNombre());
        venue.setDireccion(dto.getDireccion());
        venue.setCapacidad(dto.getCapacidad());
        venue.setCiudad(dto.getCiudad());

        return venue;
    }

    /**
     * Convert Venue entity to VenueResponseDTO.
     */
    public VenueResponseDTO toResponseDTO(Venue entity) {
        if (entity == null) {
            return null;
        }

        VenueResponseDTO dto = new VenueResponseDTO();
        dto.setId(entity.getId());
        dto.setNombre(entity.getNombre());
        dto.setDireccion(entity.getDireccion());
        dto.setCapacidad(entity.getCapacidad());
        dto.setCiudad(entity.getCiudad());

        return dto;
    }
}
