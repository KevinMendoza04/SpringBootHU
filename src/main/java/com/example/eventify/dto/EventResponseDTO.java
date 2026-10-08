package com.example.eventify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * EventResponseDTO - Data Transfer Object for event responses.
 * Contains only fields that should be exposed to clients in API responses.
 * Denormalizes related entity names to flatten the structure and protect internal IDs.
 * Does not expose audit fields or internal technical details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "EventResponseDTO",
    title = "Event Response",
    description = "Event information exposed to API clients (safe denormalization)"
)
public class EventResponseDTO {

    @Schema(description = "Unique event identifier", example = "1")
    private Long id;

    @Schema(description = "Event name/title", example = "Concierto de Rock 2024")
    private String nombre;

    @Schema(description = "Event date", example = "2024-12-25", type = "string", format = "date")
    private LocalDate fecha;

    @Schema(description = "Event description", example = "Un emocionante concierto...")
    private String descripcion;

    @Schema(
        description = "Venue name where the event is held (denormalized from Venue entity)",
        example = "Auditorio Nacional"
    )
    private String venueName;

    @Schema(
        description = "Set of category names associated with the event (denormalized from Category entities)",
        example = "[\"Música\", \"Entretenimiento\"]",
        type = "array"
    )
    private Set<String> categoryNames = new HashSet<>();
}
