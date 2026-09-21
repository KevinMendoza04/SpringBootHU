package com.example.eventify.repository;

import com.example.eventify.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    List<Venue> findByNombreContaining(String nombre);

}