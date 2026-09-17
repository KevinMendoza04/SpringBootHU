package com.example.eventify.configuration;

import com.example.eventify.model.Event;
import com.example.eventify.model.Venue;
import com.example.eventify.service.EventService;
import com.example.eventify.service.VenueService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    private final EventService eventService;
    private final VenueService venueService;

    public DataInitializer(EventService eventService, VenueService venueService) {
        this.eventService = eventService;
        this.venueService = venueService;
    }

    @Bean
    CommandLineRunner cargarDatosIniciales() {
        return args -> {
            eventService.crear(
                    new Event(
                            null,
                            "Concierto de rock",
                            LocalDate.of(2026, 10, 20),
                            "Música en vivo"
                    )
            );

            venueService.crear(
                    new Venue(
                            null,
                            "Teatro Central",
                            "Calle 10 #25-30",
                            800
                    )
            );
        };
    }
}