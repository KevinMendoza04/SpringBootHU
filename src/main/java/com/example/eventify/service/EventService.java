package com.example.eventify.service;

import com.example.eventify.exception.InvalidEventException;
import com.example.eventify.model.Event;
import com.example.eventify.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> listar() {
        return eventRepository.listar();
    }

    public Event crear(Event event) {
        if (event == null || event.getNombre() == null || event.getNombre().isBlank()) {
            throw new InvalidEventException("El nombre del evento es obligatorio");
        }

        return eventRepository.guardar(event);
    }
}