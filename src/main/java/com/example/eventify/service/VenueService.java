package com.example.eventify.service;

import com.example.eventify.model.Venue;
import com.example.eventify.repository.VenueRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.eventify.exception.ResourceNotFoundException;

@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public Venue crear(Venue venue) {

        if (venue.getNombre() == null || venue.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del lugar es obligatorio");
        }

        if (venue.getCapacidad() == null || venue.getCapacidad() <= 0) {
            throw new IllegalArgumentException("La capacidad del lugar debe ser mayor a cero");
        }

        return venueRepository.save(venue);
    }

    public Page<Venue> listar(Pageable pageable) {
        return venueRepository.findAll(pageable);
    }

    public Venue buscarPorId(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lugar no encontrado con ID: " + id));
    }

    public Venue actualizar(Long id, Venue venue) {

        Venue existente = buscarPorId(id);

        existente.setNombre(venue.getNombre());
        existente.setDireccion(venue.getDireccion());
        existente.setCapacidad(venue.getCapacidad());

        return venueRepository.save(existente);
    }

    public void eliminar(Long id) {

        buscarPorId(id);

        venueRepository.deleteById(id);
    }
}