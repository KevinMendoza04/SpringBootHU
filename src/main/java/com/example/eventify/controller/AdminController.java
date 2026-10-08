package com.example.eventify.controller;

import com.example.eventify.dto.EventCreateDTO;
import com.example.eventify.dto.VenueCreateDTO;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

/**
 * AdminController - Web UI controller for administrative event, venue, and category management.
 * 
 * Features:
 * - Dashboard with paginated listings
 * - Event creation with venue selection and category assignment
 * - Venue management with validation feedback
 * - Advanced search with filter persistence in URL
 * - Soft delete operations
 * - Visual validation error feedback via Thymeleaf BindingResult
 * - Flash messages for user feedback
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
        if (!model.containsAttribute("eventoDTO")) {
            model.addAttribute("eventoDTO", new EventCreateDTO());
        }
        model.addAttribute("lugares", venueService.listar());
        model.addAttribute("categorias", categoryService.listar());
        return "admin/nuevo-evento";
    }

    /**
     * Save a new event from form submission with validation.
     * Uses @Valid for DTO validation and BindingResult for error feedback.
     * Assigns venue (required) and categories (optional) to event.
     * Implements PRG (Post-Redirect-Get) pattern with flash attributes.
     * 
     * @param eventoDTO EventCreateDTO from form with validation
     * @param bindingResult Validation result with errors
     * @param categoriaIds Comma-separated category IDs
     * @param redirectAttributes Flash attributes for messaging
     * @return Redirect or forward based on validation result
     */
    @PostMapping("/eventos")
    public String guardarEvento(
        @Valid @ModelAttribute("eventoDTO") EventCreateDTO eventoDTO,
        BindingResult bindingResult,
        @RequestParam(required = false) String categoriaIds,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.eventoDTO", bindingResult);
            redirectAttributes.addFlashAttribute("eventoDTO", eventoDTO);
            redirectAttributes.addFlashAttribute("errorMessage", "Por favor, corrija los errores en el formulario");
            return "redirect:/admin/eventos/nuevo";
        }

        try {
            var savedEventDTO = eventService.crear(eventoDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Evento creado exitosamente: " + savedEventDTO.getNombre());
            return "redirect:/admin/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear el evento: " + e.getMessage());
            redirectAttributes.addFlashAttribute("eventoDTO", eventoDTO);
            return "redirect:/admin/eventos/nuevo";
        }
    }

    /**
     * Soft delete an event (logical deletion).
     * 
     * @param id Event ID to delete
     * @param redirectAttributes Flash attributes for messaging
     * @return Redirect to admin dashboard
     */
    @PostMapping("/eventos/{id}/eliminar")
    public String eliminarEvento(
        @PathVariable Long id,
        RedirectAttributes redirectAttributes
    ) {
        try {
            eventService.softDeletear(id);
            redirectAttributes.addFlashAttribute("successMessage", "Evento eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el evento: " + e.getMessage());
        }
        return "redirect:/admin/";
    }

    // ── Lugares ──────────────────────────────────────────────────────────────
    /**
     * Display form for creating a new venue with validation feedback.
     * 
     * @param model Spring MVC model
     * @return admin/nuevo-lugar template
     */
    @GetMapping("/lugares/nuevo")
    public String nuevoLugarForm(Model model) {
        if (!model.containsAttribute("venueDTO")) {
            model.addAttribute("venueDTO", new VenueCreateDTO());
        }
        return "admin/nuevo-lugar";
    }

    /**
     * Save a new venue from form submission with validation.
     * Uses @Valid for DTO validation and BindingResult for error feedback.
     * Implements PRG (Post-Redirect-Get) pattern with flash attributes.
     * 
     * @param venueDTO VenueCreateDTO from form with validation
     * @param bindingResult Validation result with errors
     * @param redirectAttributes Flash attributes for messaging
     * @return Redirect or forward based on validation result
     */
    @PostMapping("/lugares")
    public String guardarLugar(
        @Valid @ModelAttribute("venueDTO") VenueCreateDTO venueDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.venueDTO", bindingResult);
            redirectAttributes.addFlashAttribute("venueDTO", venueDTO);
            redirectAttributes.addFlashAttribute("errorMessage", "Por favor, corrija los errores en el formulario");
            return "redirect:/admin/lugares/nuevo";
        }

        try {
            var savedVenueDTO = venueService.crear(venueDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Lugar creado exitosamente: " + savedVenueDTO.getNombre());
            return "redirect:/admin/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear el lugar: " + e.getMessage());
            redirectAttributes.addFlashAttribute("venueDTO", venueDTO);
            return "redirect:/admin/lugares/nuevo";
        }
    }

    /**
     * Display form for updating an existing venue.
     * 
     * @param id Venue ID to update
     * @param model Spring MVC model
     * @return admin/nuevo-lugar template (reused for edit)
     */
    @GetMapping("/lugares/{id}/editar")
    public String editarLugarForm(
        @PathVariable Long id,
        Model model
    ) {
        var venueDTO = venueService.obtenerPorId(id);
        if (venueDTO.isPresent()) {
            model.addAttribute("venueDTO", venueDTO.get());
        }
        return "admin/nuevo-lugar";
    }

    /**
     * Update an existing venue with validation.
     * 
     * @param id Venue ID to update
     * @param venueDTO Updated VenueCreateDTO with validation
     * @param bindingResult Validation result with errors
     * @param redirectAttributes Flash attributes for messaging
     * @return Redirect based on validation result
     */
    @PutMapping("/lugares/{id}")
    public String actualizarLugar(
        @PathVariable Long id,
        @Valid @ModelAttribute("venueDTO") VenueCreateDTO venueDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.venueDTO", bindingResult);
            redirectAttributes.addFlashAttribute("venueDTO", venueDTO);
            redirectAttributes.addFlashAttribute("errorMessage", "Por favor, corrija los errores en el formulario");
            return "redirect:/admin/lugares/" + id + "/editar";
        }

        try {
            var updatedVenueDTO = venueService.actualizar(id, venueDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Lugar actualizado exitosamente: " + updatedVenueDTO.getNombre());
            return "redirect:/admin/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el lugar: " + e.getMessage());
            redirectAttributes.addFlashAttribute("venueDTO", venueDTO);
            return "redirect:/admin/lugares/" + id + "/editar";
        }
    }

    /**
     * Delete a venue.
     * 
     * @param id Venue ID to delete
     * @param redirectAttributes Flash attributes for messaging
     * @return Redirect to admin dashboard
     */
    @PostMapping("/lugares/{id}/eliminar")
    public String eliminarLugar(
        @PathVariable Long id,
        RedirectAttributes redirectAttributes
    ) {
        try {
            venueService.eliminar(id);
            redirectAttributes.addFlashAttribute("successMessage", "Lugar eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el lugar: " + e.getMessage());
        }
        return "redirect:/admin/";
    }

    // ── Categorías ───────────────────────────────────────────────────────────
    /**
     * Display form for creating a new category.
     * 
     * @param model Spring MVC model
     * @return admin/nueva-categoria template
     */
    @GetMapping("/categorias/nuevo")
    public String nuevaCategoriaForm(Model model) {
        model.addAttribute("categoria", new Category());
        return "admin/nueva-categoria";
    }

    /**
     * Save a new category from form submission.
     * 
     * @param categoria Category object from form
     * @param redirectAttributes Flash attributes for messaging
     * @return Redirect to admin dashboard
     */
    @PostMapping("/categorias")
    public String guardarCategoria(
        @ModelAttribute("categoria") Category categoria,
        RedirectAttributes redirectAttributes
    ) {
        try {
            categoryService.crear(categoria);
            redirectAttributes.addFlashAttribute("successMessage", "Categoría creada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear la categoría: " + e.getMessage());
        }
        return "redirect:/admin/";
    }
}
