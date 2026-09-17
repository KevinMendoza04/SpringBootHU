package com.example.eventify.repository;

import com.example.eventify.model.Event;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class EventRepository {

    private final List<Event> eventos = new ArrayList<>();
    private Long siguienteId = 1L;

    public List<Event> listar() {
        return new ArrayList<>(eventos);
    }

    public Event guardar(Event event) {
        if (event.getId() == null) {
            event.setId(siguienteId++);
        }

        eventos.add(event);
        return event;
    }
}