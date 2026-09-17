package com.example.eventify.service;

import com.example.eventify.exception.InvalidVenueException;
import com.example.eventify.model.Venue;
import com.example.eventify.repository.VenueRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public List<Venue> listar() {
        return venueRepository.listar();
    }

    public Venue crear(Venue venue) {
        if (venue == null || venue.getNombre() == null || venue.getNombre().isBlank()) {
            throw new InvalidVenueException("El nombre del lugar es obligatorio");
        }

        if (venue.getCapacidad() == null || venue.getCapacidad() <= 0) {
            throw new InvalidVenueException("La capacidad debe ser mayor que cero");
        }

        return venueRepository.guardar(venue);
    }
}