package com.example.eventify.controller;

import com.example.eventify.model.Category;
import com.example.eventify.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CategoryController - REST API for event category management.
 * 
 * Features:
 * - CRUD operations for categories
 * - Search by name with case-insensitive matching
 * - Unique constraint on category names
 * - Comprehensive Swagger/OpenAPI documentation
 */
@Tag(name = "Categorías", description = "Operaciones para consultar, crear y gestionar categorías temáticas de eventos")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * List all categories.
     * 
     * @return List of all Category entities
     */
    @GetMapping
    @Operation(
        summary = "Listar todas las categorías",
        description = "Recupera el listado completo de categorías temáticas disponibles para clasificar eventos."
    )
    @ApiResponse(responseCode = "200", description = "Listado de categorías recuperado")
    public ResponseEntity<List<Category>> listar() {
        return ResponseEntity.ok(categoryService.listar());
    }

    /**
     * Get category details by ID.
     * 
     * @param id Category ID
     * @return Category details
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener detalles de una categoría",
        description = "Recupera la información de una categoría específica."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<Category> obtenerPorId(
        @Parameter(description = "ID de la categoría", example = "1")
        @PathVariable Long id
    ) {
        return categoryService.obtenerPorId(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Search category by name (case-insensitive).
     * 
     * @param nombre Category name
     * @return Category if found
     */
    @GetMapping("/buscar/nombre")
    @Operation(
        summary = "Buscar categoría por nombre",
        description = "Busca una categoría por nombre con búsqueda insensible a mayúsculas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<Category> obtenerPorNombre(
        @Parameter(description = "Nombre de la categoría", example = "conciertos")
        @RequestParam String nombre
    ) {
        return categoryService.obtenerPorNombre(nombre)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create a new category.
     * Category names must be unique (enforced by database constraint).
     * 
     * @param category Category object with name and optional description
     * @return Created Category with assigned ID
     */
    @PostMapping
    @Operation(
        summary = "Crear categoría",
        description = "Crea una nueva categoría temática. Los nombres de categorías deben ser únicos."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente", content = @Content(schema = @Schema(implementation = Category.class))),
        @ApiResponse(responseCode = "400", description = "Datos de categoría inválidos o nombre duplicado")
    })
    public ResponseEntity<Category> crear(@RequestBody Category category) {
        Category categoryCreada = categoryService.crear(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryCreada);
    }

    /**
     * Update an existing category.
     * 
     * @param id Category ID to update
     * @param category Updated Category data
     * @return Updated Category
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar categoría",
        description = "Actualiza los datos de una categoría existente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categoría actualizada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<Category> actualizar(
        @Parameter(description = "ID de la categoría", example = "1")
        @PathVariable Long id,
        @RequestBody Category category
    ) {
        try {
            Category updated = categoryService.actualizar(id, category);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a category by ID.
     * 
     * @param id Category ID to delete
     * @return Success response
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar categoría",
        description = "Elimina una categoría del sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Categoría eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<Void> eliminar(
        @Parameter(description = "ID de la categoría", example = "1")
        @PathVariable Long id
    ) {
        try {
            categoryService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
