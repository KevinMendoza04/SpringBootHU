-- V1__Initial_Schema.sql
-- Initial database schema for Eventify

CREATE TABLE venues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    capacidad INT NOT NULL,
    ciudad VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    fecha DATE NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    venue_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_venue FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE events_categories (
    event_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (event_id, category_id),
    CONSTRAINT fk_event_category_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_event_category_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_events_venue_id ON events(venue_id);
CREATE INDEX idx_events_fecha ON events(fecha);
CREATE INDEX idx_events_is_active ON events(is_active);
CREATE INDEX idx_venues_ciudad ON venues(ciudad);
CREATE INDEX idx_events_categories_category_id ON events_categories(category_id);
