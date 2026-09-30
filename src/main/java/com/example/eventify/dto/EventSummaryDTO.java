package com.example.eventify.dto;

import java.time.LocalDate;
import java.util.Set;

/**
 * EventSummaryDTO - Record projection for efficient listing of massive event catalogs.
 * This DTO flattens the information and avoids loading full entities from memory,
 * optimizing query performance for large datasets.
 *
 * Fields:
 * - id: Event identifier
 * - nombre: Event name
 * - fecha: Event date
 * - descripcion: Event description
 * - venueName: Associated venue name (Many-to-One denormalization)
 * - venueCiudad: Venue city for geographic filtering
 * - categoryNames: Set of category names associated with the event
 */
public record EventSummaryDTO(
    Long id,
    String nombre,
    LocalDate fecha,
    String descripcion,
    String venueName,
    String venueCiudad,
    Set<String> categoryNames
) {
    /**
     * Constructor for creating an EventSummaryDTO from query results.
     * This record is designed to be populated directly from JPQL constructor expressions.
     */
}
