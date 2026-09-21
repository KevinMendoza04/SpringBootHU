package com.example.eventify.repository;

import com.example.eventify.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByNombreContaining(String nombre);

}