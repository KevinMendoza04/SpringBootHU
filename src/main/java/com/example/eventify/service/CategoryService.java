package com.example.eventify.service;

import com.example.eventify.exception.InvalidEventException;
import com.example.eventify.model.Category;
import com.example.eventify.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * CategoryService - Orchestrates business logic for event category management.
 * 
 * Key Features:
 * - Comprehensive category CRUD operations with validation
 * - Search methods for case-insensitive category lookup
 * - Transaction management for data consistency
 * - Comprehensive logging for audit trail and observability
 */
@Service
@Transactional
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Retrieve all categories from database.
     * 
     * @return List of all Category entities
     */
    public List<Category> listar() {
        logger.debug("Fetching all categories");
        return categoryRepository.findAll();
    }

    /**
     * Create a new category with validation.
     * Category names must be unique (database constraint enforced).
     * 
     * @param category Category entity to persist
     * @return Persisted Category with assigned ID
     * @throws InvalidEventException if validation fails
     */
    public Category crear(Category category) {
        logger.info("Creating new category: {}", category.getNombre());

        if (category == null || category.getNombre() == null || category.getNombre().isBlank()) {
            throw new InvalidEventException("El nombre de la categoría es obligatorio");
        }

        // Check if category already exists (case-insensitive)
        Optional<Category> existing = categoryRepository.findByNombreIgnoreCase(category.getNombre());
        if (existing.isPresent()) {
            logger.warn("Category already exists: {}", category.getNombre());
            throw new InvalidEventException("La categoría '" + category.getNombre() + "' ya existe");
        }

        Category savedCategory = categoryRepository.save(category);
        logger.info("Category created successfully with ID: {}", savedCategory.getId());

        return savedCategory;
    }

    /**
     * Find category by ID.
     * 
     * @param id Category ID
     * @return Optional containing the Category if found
     */
    public Optional<Category> obtenerPorId(Long id) {
        logger.debug("Fetching category by ID: {}", id);
        return categoryRepository.findById(id);
    }

    /**
     * Find category by name (case-insensitive).
     * 
     * @param nombre Category name
     * @return Optional containing the Category if found
     */
    public Optional<Category> obtenerPorNombre(String nombre) {
        logger.debug("Fetching category by name: {}", nombre);
        return categoryRepository.findByNombreIgnoreCase(nombre);
    }

    /**
     * Update an existing category.
     * 
     * @param id Category ID to update
     * @param categoryActualizada Updated Category data
     * @return Updated Category entity
     * @throws InvalidEventException if category not found or validation fails
     */
    public Category actualizar(Long id, Category categoryActualizada) {
        logger.info("Updating category with ID: {}", id);

        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new InvalidEventException("Categoría no encontrada con ID: " + id));

        if (categoryActualizada.getNombre() != null && !categoryActualizada.getNombre().isBlank()) {
            category.setNombre(categoryActualizada.getNombre());
        }

        if (categoryActualizada.getDescripcion() != null) {
            category.setDescripcion(categoryActualizada.getDescripcion());
        }

        Category updated = categoryRepository.save(category);
        logger.info("Category updated successfully: {}", id);

        return updated;
    }

    /**
     * Delete a category by ID.
     * 
     * @param id Category ID to delete
     * @throws InvalidEventException if category not found
     */
    public void eliminar(Long id) {
        logger.info("Deleting category with ID: {}", id);

        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new InvalidEventException("Categoría no encontrada con ID: " + id));

        categoryRepository.delete(category);
        logger.info("Category deleted successfully: {}", id);
    }
}
