package com.huongcung.core.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Component to clean up orphaned data from join tables before Hibernate
 * tries to create foreign key constraints.
 * This runs before JPA initialization to prevent foreign key constraint violations.
 */
@Component
@Slf4j
@Order(1) // Run before other components
public class DatabaseCleanupConfig implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private DataSource dataSource;
    
    private boolean cleanupExecuted = false;

    @PostConstruct
    public void cleanupOrphanedData() {
        try {
            log.info("Starting cleanup of orphaned data in join tables...");
            
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            // Clean up orphaned records in books_authors table
            int deletedBooksAuthors = jdbcTemplate.update(
                "DELETE ba FROM books_authors ba " +
                "LEFT JOIN books b ON ba.book_id = b.id " +
                "LEFT JOIN authors a ON ba.author_id = a.id " +
                "WHERE b.id IS NULL OR a.id IS NULL"
            );
            if (deletedBooksAuthors > 0) {
                log.warn("Deleted {} orphaned records from books_authors table", deletedBooksAuthors);
            }
            
            // Clean up orphaned records in books_translators table
            int deletedBooksTranslators = jdbcTemplate.update(
                "DELETE bt FROM books_translators bt " +
                "LEFT JOIN books b ON bt.book_id = b.id " +
                "LEFT JOIN translators t ON bt.translator_id = t.id " +
                "WHERE b.id IS NULL OR t.id IS NULL"
            );
            if (deletedBooksTranslators > 0) {
                log.warn("Deleted {} orphaned records from books_translators table", deletedBooksTranslators);
            }
            
            // Clean up orphaned records in books_genres table
            int deletedBooksGenres = jdbcTemplate.update(
                "DELETE bg FROM books_genres bg " +
                "LEFT JOIN books b ON bg.book_id = b.id " +
                "LEFT JOIN genres g ON bg.genre_id = g.id " +
                "WHERE b.id IS NULL OR g.id IS NULL"
            );
            if (deletedBooksGenres > 0) {
                log.warn("Deleted {} orphaned records from books_genres table", deletedBooksGenres);
            }
            
            // Clean up orphaned records in book_images table
            int deletedBookImages = jdbcTemplate.update(
                "DELETE bi FROM book_images bi " +
                "LEFT JOIN books b ON bi.book_id = b.id " +
                "WHERE b.id IS NULL"
            );
            if (deletedBookImages > 0) {
                log.warn("Deleted {} orphaned records from book_images table", deletedBookImages);
            }
            
            // Clean up orphaned records in ebooks table
            int deletedEbooks = jdbcTemplate.update(
                "DELETE e FROM ebooks e " +
                "LEFT JOIN books b ON e.book_id = b.id " +
                "WHERE b.id IS NULL"
            );
            if (deletedEbooks > 0) {
                log.warn("Deleted {} orphaned records from ebooks table", deletedEbooks);
            }
            
            // Clean up orphaned records in physical_books table
            int deletedPhysicalBooks = jdbcTemplate.update(
                "DELETE pb FROM physical_books pb " +
                "LEFT JOIN books b ON pb.book_id = b.id " +
                "WHERE b.id IS NULL"
            );
            if (deletedPhysicalBooks > 0) {
                log.warn("Deleted {} orphaned records from physical_books table", deletedPhysicalBooks);
            }
            
            // Clean up orphaned publisher references in books table
            int updatedBooks = jdbcTemplate.update(
                "UPDATE books b " +
                "LEFT JOIN publishers p ON b.publisher_id = p.id " +
                "SET b.publisher_id = NULL " +
                "WHERE b.publisher_id IS NOT NULL AND p.id IS NULL"
            );
            if (updatedBooks > 0) {
                log.warn("Updated {} books with orphaned publisher references", updatedBooks);
            }
            
            log.info("Cleanup of orphaned data completed successfully");
            cleanupExecuted = true;
            
        } catch (Exception e) {
            // Log error but don't fail startup - Hibernate will handle constraint creation
            log.error("Error during orphaned data cleanup: {}", e.getMessage(), e);
            // If tables don't exist yet, that's okay - Hibernate will create them
            if (e.getMessage() != null && (e.getMessage().contains("doesn't exist") || 
                e.getMessage().contains("Unknown table"))) {
                log.info("Tables don't exist yet, skipping cleanup. Hibernate will create them.");
            }
        }
    }
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Ensure cleanup runs if it hasn't already
        if (!cleanupExecuted) {
            cleanupOrphanedData();
        }
    }
}

