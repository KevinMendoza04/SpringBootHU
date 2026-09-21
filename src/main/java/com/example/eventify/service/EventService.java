package com.example.eventify.service;

import com.example.eventify.model.Event;
import com.example.eventify.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.eventify.exception.ResourceNotFoundException;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event crear(Event event) {

        if (event.getNombre() == null || event.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del evento es obligatorio");
        }

        return eventRepository.save(event);
    }

    public Page<Event> listar(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

    public Event buscarPorId(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Evento no encontrado con ID: " + id));
    }

    public Event actualizar(Long id, Event event) {

        Event existente = buscarPorId(id);

        existente.setNombre(event.getNombre());
        existente.setFecha(event.getFecha());
        existente.setDescripcion(event.getDescripcion());

        return eventRepository.save(existente);
    }

    public void eliminar(Long id) {

        buscarPorId(id);

        eventRepository.deleteById(id);
    }
}