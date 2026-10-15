package com.example.eventify.repository;

import com.example.eventify.model.Event;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
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
     * Fetches associations required for an event summary. The service builds the
     * category-name Set because JPQL cannot construct a Java collection from a subquery.
     */
    @EntityGraph(attributePaths = {"venue", "categories"})
    Slice<Event> findByIsActiveTrueOrderByFechaDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"venue", "categories"})
    Slice<Event> findByVenueCiudadIgnoreCaseContainingOrderByFechaDesc(String ciudad, Pageable pageable);

    @EntityGraph(attributePaths = {"venue", "categories"})
    Slice<Event> findDistinctByCategoriesNombreIgnoreCaseContainingOrderByFechaDesc(
        String categoryName,
        Pageable pageable
    );

    @EntityGraph(attributePaths = {"venue", "categories"})
    Slice<Event> findDistinctByVenueCiudadIgnoreCaseContainingAndCategoriesNombreIgnoreCaseContainingOrderByFechaDesc(
        String ciudad,
        String categoryName,
        Pageable pageable
    );
}
