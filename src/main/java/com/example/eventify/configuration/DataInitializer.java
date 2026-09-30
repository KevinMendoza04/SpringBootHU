package com.example.eventify.configuration;

import org.springframework.context.annotation.Configuration;

/**
 * DataInitializer - Note: Disabled in favor of Flyway migrations
 * 
 * Flyway manages database schema and seed data via migration scripts:
 * - V1__Initial_Schema.sql: Creates initial tables
 * - V2__Add_Audit_Columns.sql: Adds audit tracking
 * - V3__Seed_Data_Massive.sql: Seeds 200+ test records
 */
@Configuration
public class DataInitializer {
    // Flyway handles all data initialization
}
