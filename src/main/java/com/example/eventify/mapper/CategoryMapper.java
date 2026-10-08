package com.example.eventify.mapper;

import com.example.eventify.model.Category;
import org.mapstruct.Mapper;

/**
 * CategoryMapper - MapStruct mapper interface for Category entity.
 * Handles mappings for Category denormalization in Event responses.
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    /**
     * Map a single Category to its name.
     */
    default String categoryToName(Category category) {
        return category == null ? null : category.getNombre();
    }
}
