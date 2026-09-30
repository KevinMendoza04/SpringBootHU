package com.example.eventify.repository;

import com.example.eventify.model.Event;
import com.example.eventify.dto.EventSummaryDTO;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @EntityGraph(attributePaths = {"venue", "categories"})
    Slice<Event> findAllByOrderByFechaDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"venue", "categories"})
    List<Event> findByVenueCiudadIgnoreCaseContaining(String ciudad);

    @EntityGraph(attributePaths = {"venue", "categories"})
    List<Event> findByCategoriesNombreIgnoreCaseContaining(String categoryName);

    @EntityGraph(attributePaths = {"venue", "categories"})
    List<Event> findByFechaBetween(LocalDate startDate, LocalDate endDate);

    @EntityGraph(attributePaths = {"venue", "categories"})
    List<Event> findByVenueCapacidadGreaterThanEqual(Integer minCapacity);

    @EntityGraph(attributePaths = {"venue", "categories"})
    List<Event> findByVenueCiudadIgnoreCaseContainingAndCategoriesNombreIgnoreCaseContaining(String ciudad, String categoryName);

    @EntityGraph(attributePaths = {"venue", "categories"})
    List<Event> findByNombreIgnoreCaseContainingOrderByFechaDesc(String nombre);

    /**
     * Efficient query using Record projection to retrieve event summaries without loading full entities.
     * Uses JPQL constructor expression to map results directly to EventSummaryDTO Record.
     * Implements Slice pagination to avoid counting total records (optimizes for large datasets).
     * 
     * @param pageable Slice pagination parameters
     * @return Slice of EventSummaryDTO objects ordered by date descending
     */
    @Query("""
        SELECT new com.example.eventify.dto.EventSummaryDTO(
            e.id,
            e.nombre,
            e.fecha,
            e.descripcion,
            e.venue.nombre,
            e.venue.ciudad,
            CAST(
                (SELECT DISTINCT c.nombre FROM e.categories c) AS java.util.Set
            )
        )
        FROM Event e
        WHERE e.isActive = true
        ORDER BY e.fecha DESC
        """)
    Slice<EventSummaryDTO> findAllEventSummaries(Pageable pageable);

    /**
     * Query event summaries filtered by city.
     * Efficiently retrieves denormalized event data without N+1 query problems.
     * 
     * @param ciudad The city name to filter by (case-insensitive)
     * @param pageable Slice pagination parameters
     * @return Slice of filtered EventSummaryDTO objects
     */
    @Query("""
        SELECT new com.example.eventify.dto.EventSummaryDTO(
            e.id,
            e.nombre,
            e.fecha,
            e.descripcion,
            e.venue.nombre,
            e.venue.ciudad,
            CAST(
                (SELECT DISTINCT c.nombre FROM e.categories c) AS java.util.Set
            )
        )
        FROM Event e
        WHERE e.isActive = true AND LOWER(e.venue.ciudad) LIKE LOWER(CONCAT('%', :ciudad, '%'))
        ORDER BY e.fecha DESC
        """)
    Slice<EventSummaryDTO> findEventSummariesByCity(@Param("ciudad") String ciudad, Pageable pageable);

    /**
     * Query event summaries filtered by category.
     * Uses joins efficiently with denormalization to avoid N+1 problems.
     * 
     * @param categoryName The category name to filter by (case-insensitive)
     * @param pageable Slice pagination parameters
     * @return Slice of filtered EventSummaryDTO objects
     */
    @Query("""
        SELECT DISTINCT new com.example.eventify.dto.EventSummaryDTO(
            e.id,
            e.nombre,
            e.fecha,
            e.descripcion,
            e.venue.nombre,
            e.venue.ciudad,
            CAST(
                (SELECT DISTINCT c.nombre FROM e.categories c) AS java.util.Set
            )
        )
        FROM Event e
        JOIN e.categories c
        WHERE e.isActive = true AND LOWER(c.nombre) LIKE LOWER(CONCAT('%', :categoryName, '%'))
        ORDER BY e.fecha DESC
        """)
    Slice<EventSummaryDTO> findEventSummariesByCategory(@Param("categoryName") String categoryName, Pageable pageable);

    /**
     * Query event summaries with combined city and category filters.
     * Optimized single query avoids multiple roundtrips to database.
     * 
     * @param ciudad The city name to filter by (case-insensitive)
     * @param categoryName The category name to filter by (case-insensitive)
     * @param pageable Slice pagination parameters
     * @return Slice of filtered EventSummaryDTO objects
     */
    @Query("""
        SELECT DISTINCT new com.example.eventify.dto.EventSummaryDTO(
            e.id,
            e.nombre,
            e.fecha,
            e.descripcion,
            e.venue.nombre,
            e.venue.ciudad,
            CAST(
                (SELECT DISTINCT c.nombre FROM e.categories c) AS java.util.Set
            )
        )
        FROM Event e
        JOIN e.categories c
        WHERE e.isActive = true 
            AND LOWER(e.venue.ciudad) LIKE LOWER(CONCAT('%', :ciudad, '%'))
            AND LOWER(c.nombre) LIKE LOWER(CONCAT('%', :categoryName, '%'))
        ORDER BY e.fecha DESC
        """)
    Slice<EventSummaryDTO> findEventSummariesByCityAndCategory(
        @Param("ciudad") String ciudad,
        @Param("categoryName") String categoryName,
        Pageable pageable
    );
}
