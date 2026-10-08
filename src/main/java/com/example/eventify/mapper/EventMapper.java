package com.example.eventify.mapper;

import com.example.eventify.dto.EventCreateDTO;
import com.example.eventify.dto.EventResponseDTO;
import com.example.eventify.model.Category;
import com.example.eventify.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * EventMapper - Mapper for Event entity and DTOs.
 * Handles conversion between:
 * - Event (JPA entity) <-> EventCreateDTO (input)
 * - Event (JPA entity) <-> EventResponseDTO (output)
 */
@Component
@Mapper(componentModel = "spring")
public abstract class EventMapper {

    /**
     * Convert EventCreateDTO to Event entity.
     */
    public Event toEntity(EventCreateDTO dto) {
        if (dto == null) {
            return null;
        }

        Event event = new Event();
        event.setNombre(dto.getNombre());
        event.setFecha(dto.getFecha());
        event.setDescripcion(dto.getDescripcion());
        event.setIsActive(true);

        return event;
    }

    /**
     * Convert Event entity to EventResponseDTO.
     */
    public EventResponseDTO toResponseDTO(Event event) {
        if (event == null) {
            return null;
        }

        EventResponseDTO dto = new EventResponseDTO();
        dto.setId(event.getId());
        dto.setNombre(event.getNombre());
        dto.setFecha(event.getFecha());
        dto.setDescripcion(event.getDescripcion());
        
        if (event.getVenue() != null) {
            dto.setVenueName(event.getVenue().getNombre());
        }
        
        dto.setCategoryNames(mapCategoryNames(event.getCategories()));

        return dto;
    }

    /**
     * Map categories collection to category names.
     */
    @Named("categoriesToNames")
    public Set<String> mapCategoryNames(Set<Category> categories) {
        if (categories == null) {
            return new HashSet<>();
        }
        return categories.stream()
            .map(Category::getNombre)
            .collect(Collectors.toSet());
    }
}
