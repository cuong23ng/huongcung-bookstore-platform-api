-- Cleanup script to remove orphaned data from join tables
-- This script removes records from join tables that reference non-existent parent records
-- Run this before Hibernate tries to create foreign key constraints

USE huongcungbookstore;

-- Clean up orphaned records in books_authors table
DELETE ba FROM books_authors ba
LEFT JOIN books b ON ba.book_id = b.id
LEFT JOIN authors a ON ba.author_id = a.id
WHERE b.id IS NULL OR a.id IS NULL;

-- Clean up orphaned records in books_translators table
DELETE bt FROM books_translators bt
LEFT JOIN books b ON bt.book_id = b.id
LEFT JOIN translators t ON bt.translator_id = t.id
WHERE b.id IS NULL OR t.id IS NULL;

-- Clean up orphaned records in books_genres table
DELETE bg FROM books_genres bg
LEFT JOIN books b ON bg.book_id = b.id
LEFT JOIN genres g ON bg.genre_id = g.id
WHERE b.id IS NULL OR g.id IS NULL;

-- Clean up orphaned records in book_images table
DELETE bi FROM book_images bi
LEFT JOIN books b ON bi.book_id = b.id
WHERE b.id IS NULL;

-- Clean up orphaned records in ebooks table
DELETE e FROM ebooks e
LEFT JOIN books b ON e.book_id = b.id
WHERE b.id IS NULL;

-- Clean up orphaned records in physical_books table
DELETE pb FROM physical_books pb
LEFT JOIN books b ON pb.book_id = b.id
WHERE b.id IS NULL;

-- Clean up orphaned records in books table that reference non-existent publishers
UPDATE books b
LEFT JOIN publishers p ON b.publisher_id = p.id
SET b.publisher_id = NULL
WHERE b.publisher_id IS NOT NULL AND p.id IS NULL;




