package com.huongcung.core.search.service;

import com.huongcung.core.product.model.entity.AbstractBookEntity;

/**
 * Service interface for indexing books into Solr
 */
public interface SearchIndexService {
    
    /**
     * Index a single book into Solr
     * 
     * @param book Book entity to index
     * @return true if indexing succeeded, false otherwise
     */
    boolean indexBook(AbstractBookEntity book);
    
    /**
     * Index all books from the database into Solr
     * Processes books in batches for performance
     * 
     * @return IndexingResult containing success/failure counts
     */
    IndexingResult indexAllBooks();
    
    /**
     * Update the index for a specific book by ID
     * 
     * @param bookId Book ID to update
     * @return true if update succeeded, false otherwise
     */
    boolean updateBookIndex(Long bookId);
    
    /**
     * Delete a book from the Solr index
     * 
     * @param bookId Book ID to delete
     * @return true if deletion succeeded, false otherwise
     */
    boolean deleteBookFromIndex(Long bookId);
    
    /**
     * Result of bulk indexing operation
     */
    class IndexingResult {
        private final long totalBooks;
        private final long indexedCount;
        private final long errorCount;
        private final long durationMs;
        
        public IndexingResult(long totalBooks, long indexedCount, long errorCount, long durationMs) {
            this.totalBooks = totalBooks;
            this.indexedCount = indexedCount;
            this.errorCount = errorCount;
            this.durationMs = durationMs;
        }
        
        public long getTotalBooks() {
            return totalBooks;
        }
        
        public long getIndexedCount() {
            return indexedCount;
        }
        
        public long getErrorCount() {
            return errorCount;
        }
        
        public long getDurationMs() {
            return durationMs;
        }
        
        public double getSuccessRate() {
            return totalBooks > 0 ? (double) indexedCount / totalBooks * 100 : 0.0;
        }
    }
}

