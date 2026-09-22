package com.example.eventify.controller;

import com.example.eventify.model.Event;
import com.example.eventify.model.Venue;
import com.example.eventify.service.EventService;
import com.example.eventify.service.VenueService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final EventService eventService;
    private final VenueService venueService;

    public AdminController(EventService eventService, VenueService venueService) {
        this.eventService = eventService;
        this.venueService = venueService;
    }

    // ── Panel principal ──────────────────────────────────────────────────────

    @GetMapping({"/", ""})
    public String panel(Model model) {
        model.addAttribute("eventos", eventService.listar());
        model.addAttribute("lugares", venueService.listar());
        return "admin/index";
    }

    // ── Eventos ──────────────────────────────────────────────────────────────

    @GetMapping("/eventos/nuevo")
    public String nuevoEventoForm(Model model) {
        model.addAttribute("evento", new Event());
        return "admin/nuevo-evento";
    }

    /**
     * Recibe el formulario de evento, persiste y redirige al panel (PRG).
     */
    @PostMapping("/eventos")
    public String guardarEvento(@ModelAttribute("evento") Event evento) {
        eventService.crear(evento);
        return "redirect:/admin/";
    }

    // ── Lugares ──────────────────────────────────────────────────────────────

    @GetMapping("/lugares/nuevo")
    public String nuevoLugarForm(Model model) {
        model.addAttribute("venue", new Venue());
        return "admin/nuevo-lugar";
    }

    /**
     * Recibe el formulario de lugar, persiste y redirige al panel (PRG).
     */
    @PostMapping("/lugares")
    public String guardarLugar(@ModelAttribute("venue") Venue venue) {
        venueService.crear(venue);
        return "redirect:/admin/";
    }
}
