package com.example.eventify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * VenueCreateDTO - Data Transfer Object for creating venues.
 * Contains only fields that clients send to the API for venue creation.
 * Excludes ID and other server-managed fields.
 * Includes comprehensive validation rules for input data quality.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "VenueCreateDTO",
    title = "Venue Creation Request",
    description = "Data Transfer Object for creating new venues with validation rules"
)
public class VenueCreateDTO {

    @NotBlank(message = "El nombre del lugar es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Schema(
        description = "Venue name",
        example = "Auditorio Nacional",
        minLength = 3,
        maxLength = 100
    )
    private String nombre;

    @NotBlank(message = "La dirección del lugar es obligatoria")
    @Size(min = 5, max = 255, message = "La dirección debe tener entre 5 y 255 caracteres")
    @Schema(
        description = "Physical address of the venue",
        example = "Calle Principal 123, Piso 5",
        minLength = 5,
        maxLength = 255
    )
    private String direccion;

    @NotNull(message = "La capacidad del lugar es obligatoria")
    @Positive(message = "La capacidad debe ser mayor que cero")
    @Max(value = 1000000, message = "La capacidad no puede exceder 1,000,000")
    @Schema(
        description = "Maximum capacity of the venue",
        example = "5000",
        type = "integer",
        minimum = "1",
        maximum = "1000000"
    )
    private Integer capacidad;

    @NotBlank(message = "La ciudad del lugar es obligatoria")
    @Size(min = 2, max = 100, message = "La ciudad debe tener entre 2 y 100 caracteres")
    @Schema(
        description = "City where the venue is located",
        example = "Bogotá",
        minLength = 2,
        maxLength = 100
    )
    private String ciudad;
}
