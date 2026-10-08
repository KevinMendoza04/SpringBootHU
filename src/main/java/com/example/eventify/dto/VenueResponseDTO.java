package com.example.eventify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * VenueResponseDTO - Data Transfer Object for venue responses.
 * Contains only fields that should be exposed to clients in API responses.
 * Does not expose internal technical details or audit fields.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "VenueResponseDTO",
    title = "Venue Response",
    description = "Venue information exposed to API clients"
)
public class VenueResponseDTO {

    @Schema(description = "Unique venue identifier", example = "1")
    private Long id;

    @Schema(description = "Venue name", example = "Auditorio Nacional")
    private String nombre;

    @Schema(description = "Physical address", example = "Calle Principal 123, Piso 5")
    private String direccion;

    @Schema(description = "Maximum capacity", example = "5000")
    private Integer capacidad;

    @Schema(description = "City location", example = "Bogotá")
    private String ciudad;
}
