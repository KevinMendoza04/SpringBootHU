package com.example.eventify.controller;

import com.example.eventify.model.Category;
import com.example.eventify.model.Event;
import com.example.eventify.model.Venue;
import com.example.eventify.service.CategoryService;
import com.example.eventify.service.EventService;
import com.example.eventify.service.VenueService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * AdminController - Web UI controller for administrative event, venue, and category management.
 * 
 * Features:
 * - Dashboard with paginated listings
 * - Event creation with venue selection and category assignment
 * - Venue management
 * - Advanced search with filter persistence in URL
 * - Soft delete operations
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final int PAGE_SIZE = 10;

    private final EventService eventService;
    private final VenueService venueService;
    private final CategoryService categoryService;

    public AdminController(EventService eventService, VenueService venueService, CategoryService categoryService) {
        this.eventService = eventService;
        this.venueService = venueService;
        this.categoryService = categoryService;
    }

    // ── Panel principal ──────────────────────────────────────────────────────

    /**
     * Display main admin dashboard with paginated events and venues.
     * 
     * @param page Page number (0-indexed)
     * @param model Spring MVC model
     * @return admin/index template
     */
    @GetMapping({"/", ""})
    public String panel(
        @RequestParam(defaultValue = "0") int page,
        Model model
    ) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Slice<Event> eventosSlice = eventService.listarEventos(pageable);
        
        model.addAttribute("eventos", eventosSlice.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("hasNext", eventosSlice.hasNext());
        model.addAttribute("hasPrevious", page > 0);
        model.addAttribute("lugares", venueService.listar());
        model.addAttribute("categorias", categoryService.listar());
        
        return "admin/index";
    }

    /**
     * Display admin dashboard with advanced search filters.
     * Supports searching by city and/or category with pagination.
     * 
     * @param ciudad Optional city filter
     * @param categoria Optional category filter
     * @param page Page number
     * @param model Spring MVC model
     * @return admin/index template
     */
    @GetMapping("/buscar")
    public String buscar(
        @RequestParam(required = false) String ciudad,
        @RequestParam(required = false) String categoria,
        @RequestParam(defaultValue = "0") int page,
        Model model
    ) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Slice<?> resultados;

        if (ciudad != null && !ciudad.isBlank() && categoria != null && !categoria.isBlank()) {
            resultados = eventService.buscarPorCiudadYCategoria(ciudad, categoria, pageable);
            model.addAttribute("filtro", "ciudad: " + ciudad + ", categoría: " + categoria);
        } else if (ciudad != null && !ciudad.isBlank()) {
            resultados = eventService.buscarPorCiudad(ciudad, pageable);
            model.addAttribute("filtro", "ciudad: " + ciudad);
        } else if (categoria != null && !categoria.isBlank()) {
            resultados = eventService.buscarPorCategoria(categoria, pageable);
            model.addAttribute("filtro", "categoría: " + categoria);
        } else {
            Pageable pageableEvents = PageRequest.of(page, PAGE_SIZE);
            resultados = eventService.listarEventos(pageableEvents);
            model.addAttribute("filtro", "sin filtros");
        }

        model.addAttribute("eventos", resultados.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("hasNext", resultados.hasNext());
        model.addAttribute("hasPrevious", page > 0);
        model.addAttribute("ciudad", ciudad);
        model.addAttribute("categoria", categoria);
        model.addAttribute("lugares", venueService.listar());
        model.addAttribute("categorias", categoryService.listar());

        return "admin/index";
    }

    // ── Eventos ──────────────────────────────────────────────────────────────

    /**
     * Display form for creating a new event.
     * Pre-populates venue and category lists for selection.
     * 
     * @param model Spring MVC model
     * @return admin/nuevo-evento template
     */
    @GetMapping("/eventos/nuevo")
    public String nuevoEventoForm(Model model) {
        model.addAttribute("evento", new Event());
        model.addAttribute("lugares", venueService.listar());
        model.addAttribute("categorias", categoryService.listar());
        return "admin/nuevo-evento";
    }

    /**
     * Save a new event from form submission.
     * Assigns venue (required) and categories (optional) to event.
     * Implements PRG (Post-Redirect-Get) pattern.
     * 
     * @param evento Event object from form
     * @param categoriaIds Comma-separated category IDs
     * @return Redirect to admin dashboard
     */
    @PostMapping("/eventos")
    public String guardarEvento(
        @ModelAttribute("evento") Event evento,
        @RequestParam(required = false) String categoriaIds
    ) {
        Event saved = eventService.crear(evento);

        if (categoriaIds != null && !categoriaIds.isBlank()) {
            String[] ids = categoriaIds.split(",");
            for (String idStr : ids) {
                try {
                    Long categoryId = Long.parseLong(idStr.trim());
                    categoryService.obtenerPorId(categoryId).ifPresent(cat -> 
                        saved.getCategories().add(cat)
                    );
                } catch (NumberFormatException e) {
                    // Ignore invalid category IDs
                }
            }
        }

        return "redirect:/admin/";
    }

    /**
     * Soft delete an event (logical deletion).
     * 
     * @param id Event ID to delete
     * @return Redirect to admin dashboard
     */
    @PostMapping("/eventos/{id}/eliminar")
    public String eliminarEvento(@PathVariable Long id) {
        eventService.softDeletear(id);
        return "redirect:/admin/";
    }

    // ── Lugares ──────────────────────────────────────────────────────────────

    /**
     * Display form for creating a new venue.
     * 
     * @param model Spring MVC model
     * @return admin/nuevo-lugar template
     */
    @GetMapping("/lugares/nuevo")
    public String nuevoLugarForm(Model model) {
        model.addAttribute("venue", new Venue());
        return "admin/nuevo-lugar";
    }

    /**
     * Save a new venue from form submission.
     * Implements PRG (Post-Redirect-Get) pattern.
     * 
     * @param venue Venue object from form
     * @return Redirect to admin dashboard
     */
    @PostMapping("/lugares")
    public String guardarLugar(@ModelAttribute("venue") Venue venue) {
        venueService.crear(venue);
        return "redirect:/admin/";
    }

    /**
     * Display form for creating a new category.
     * 
     * @param model Spring MVC model
     * @return admin/nueva-categoria template
     */
    @GetMapping("/categorias/nuevo")
    public String nuevaCategoriaForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/nueva-categoria";
    }

    /**
     * Save a new category from form submission.
     * Implements PRG (Post-Redirect-Get) pattern.
     * 
     * @param category Category object from form
     * @return Redirect to admin dashboard
     */
    @PostMapping("/categorias")
    public String guardarCategoria(@ModelAttribute("category") Category category) {
        categoryService.crear(category);
        return "redirect:/admin/";
    }
}
