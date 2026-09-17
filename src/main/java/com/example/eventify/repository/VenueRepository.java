package com.example.eventify.repository;

import com.example.eventify.model.Venue;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class VenueRepository {

    private final List<Venue> venues = new ArrayList<>();
    private Long siguienteId = 1L;

    public List<Venue> listar() {
        return new ArrayList<>(venues);
    }

    public Venue guardar(Venue venue) {
        if (venue.getId() == null) {
            venue.setId(siguienteId++);
        }

        venues.add(venue);
        return venue;
    }
}