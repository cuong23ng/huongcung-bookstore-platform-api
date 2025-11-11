-- Database initialization script for Huong Cung Bookstore
-- This script runs when the MS SQL Server container starts

-- Create database if it doesn't exist
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'huongcung_bookstore')
BEGIN
    CREATE DATABASE huongcung_bookstore;
END
GO

-- Use the database
USE huongcung_bookstore;
GO

-- Verify database creation
PRINT 'Database huongcung_bookstore is ready for Spring Boot application';
