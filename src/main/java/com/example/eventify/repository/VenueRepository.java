package com.example.eventify.repository;

import com.example.eventify.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    List<Venue> findByCiudadIgnoreCaseContaining(String ciudad);

    List<Venue> findByNombreIgnoreCaseContaining(String nombre);
}