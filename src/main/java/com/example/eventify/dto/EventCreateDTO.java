package com.example.eventify.dto;

import com.example.eventify.validation.NoPastEvents;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * EventCreateDTO - Data Transfer Object for creating events.
 * Contains only fields that clients send to the API for event creation.
 * Excludes ID and audit fields that are server-managed.
 * Includes comprehensive validation rules for input data quality.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "EventCreateDTO",
    title = "Event Creation Request",
    description = "Data Transfer Object for creating new events with validation rules"
)
public class EventCreateDTO {

    @NotBlank(message = "El nombre del evento es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Schema(
        description = "Event name/title",
        example = "Concierto de Rock 2024",
        minLength = 3,
        maxLength = 100
    )
    private String nombre;

    @NotNull(message = "La fecha del evento es obligatoria")
    @NoPastEvents(message = "La fecha del evento no puede ser anterior a hoy")
    @Schema(
        description = "Event date (must not be in the past)",
        example = "2024-12-25",
        type = "string",
        format = "date"
    )
    private LocalDate fecha;

    @NotBlank(message = "La descripción del evento es obligatoria")
    @Size(min = 10, max = 500, message = "La descripción debe tener entre 10 y 500 caracteres")
    @Schema(
        description = "Event description/details",
        example = "Un emocionante concierto de las mejores bandas de rock...",
        minLength = 10,
        maxLength = 500
    )
    private String descripcion;

    @NotNull(message = "El ID del lugar (venue) es obligatorio")
    @Positive(message = "El ID del lugar debe ser un número positivo")
    @Schema(
        description = "Venue ID where the event will be held",
        example = "1",
        type = "integer"
    )
    private Long venueId;

    @Size(min = 0, max = 20, message = "No se pueden asignar más de 20 categorías a un evento")
    @Schema(
        description = "Set of category IDs associated with this event (optional)",
        example = "[1, 3, 5]",
        type = "array"
    )
    private Set<Long> categoryIds = new HashSet<>();
}
