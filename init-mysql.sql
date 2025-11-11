-- MySQL initialization script to allow connections from any host
-- This script runs when the MySQL container starts for the first time

-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS huongcungbookstore;

-- Grant all privileges to root user from any host
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'hungcuong' WITH GRANT OPTION;

-- Grant all privileges to root user from localhost
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' IDENTIFIED BY 'hungcuong' WITH GRANT OPTION;

-- Grant all privileges on the specific database to root from any host
GRANT ALL PRIVILEGES ON huongcungbookstore.* TO 'root'@'%' IDENTIFIED BY 'hungcuong';

-- Grant all privileges on the specific database to root from localhost
GRANT ALL PRIVILEGES ON huongcungbookstore.* TO 'root'@'localhost' IDENTIFIED BY 'hungcuong';

-- Flush privileges to apply changes
FLUSH PRIVILEGES;

-- Use the database
USE huongcungbookstore;

-- Clean up orphaned data from join tables before Hibernate creates constraints
-- This prevents foreign key constraint violations during schema update

-- Clean up orphaned records in books_authors table (if table exists)
SET @table_exists = (SELECT COUNT(*) FROM information_schema.tables 
                     WHERE table_schema = 'huongcungbookstore' AND table_name = 'books_authors');
SET @sql = IF(@table_exists > 0,
    'DELETE ba FROM books_authors ba LEFT JOIN books b ON ba.book_id = b.id LEFT JOIN authors a ON ba.author_id = a.id WHERE b.id IS NULL OR a.id IS NULL',
    'SELECT "books_authors table does not exist, skipping cleanup" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Clean up orphaned records in books_translators table (if table exists)
SET @table_exists = (SELECT COUNT(*) FROM information_schema.tables 
                     WHERE table_schema = 'huongcungbookstore' AND table_name = 'books_translators');
SET @sql = IF(@table_exists > 0,
    'DELETE bt FROM books_translators bt LEFT JOIN books b ON bt.book_id = b.id LEFT JOIN translators t ON bt.translator_id = t.id WHERE b.id IS NULL OR t.id IS NULL',
    'SELECT "books_translators table does not exist, skipping cleanup" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Clean up orphaned records in books_genres table (if table exists)
SET @table_exists = (SELECT COUNT(*) FROM information_schema.tables 
                     WHERE table_schema = 'huongcungbookstore' AND table_name = 'books_genres');
SET @sql = IF(@table_exists > 0,
    'DELETE bg FROM books_genres bg LEFT JOIN books b ON bg.book_id = b.id LEFT JOIN genres g ON bg.genre_id = g.id WHERE b.id IS NULL OR g.id IS NULL',
    'SELECT "books_genres table does not exist, skipping cleanup" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;