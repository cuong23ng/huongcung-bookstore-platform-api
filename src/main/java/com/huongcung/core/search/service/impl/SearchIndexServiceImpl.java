package com.huongcung.core.search.service.impl;

import com.huongcung.core.catalog.model.entity.AbstractBookEntity;
import com.huongcung.core.contributor.model.entity.AuthorEntity;
import com.huongcung.core.catalog.model.entity.EbookEntity;
import com.huongcung.core.contributor.model.entity.GenreEntity;
import com.huongcung.core.catalog.model.entity.PhysicalBookEntity;
import com.huongcung.core.catalog.repository.AbstractBookRepository;
import com.huongcung.core.search.model.entity.BookSearchDocument;
import com.huongcung.core.search.repository.BookSearchRepository;
import com.huongcung.core.search.service.SearchIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of SearchIndexService for indexing books into Solr
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchIndexServiceImpl implements SearchIndexService {
    
    private final BookSearchRepository bookSearchRepository;
    private final AbstractBookRepository abstractBookRepository;
    
    @Value("${solr.indexing.batch-size:1000}")
    private int batchSize;
    
    @Override
    @Transactional
    @CacheEvict(value = {"searchResults", "searchFacets", "searchSuggestions", "frontPage"}, allEntries = true)
    public boolean indexBook(AbstractBookEntity book) {
        try {
            BookSearchDocument document = mapEntityToDocument(book);
            bookSearchRepository.index(document);
            log.debug("Successfully indexed book: {} (ID: {})", book.getTitle(), book.getId());
            return true;
        } catch (Exception e) {
            log.error("Failed to index book {} (ID: {}): {}", book.getTitle(), book.getId(), e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public IndexingResult indexAllBooks() {
        long startTime = System.currentTimeMillis();
        long totalBooks = 0;
        long indexedCount = 0;
        long errorCount = 0;
        
        try {
            log.info("Starting bulk indexing of all books...");
            
            // Fetch all books (lazy collections will be initialized in transaction)
            List<AbstractBookEntity> allBooks = abstractBookRepository.findAll();
            totalBooks = allBooks.size();
            
            // Initialize all lazy collections for all books within transaction
            for (AbstractBookEntity book : allBooks) {
                initializeLazyCollections(book);
            }
            
            if (totalBooks == 0) {
                log.warn("No books found in database to index");
                return new IndexingResult(0, 0, 0, System.currentTimeMillis() - startTime);
            }
            
            log.info("Found {} books to index. Processing in batches of {}", totalBooks, batchSize);
            
            // Process books in batches
            for (int i = 0; i < allBooks.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, allBooks.size());
                List<AbstractBookEntity> batch = allBooks.subList(i, endIndex);
                
                try {
                    // Map entities to documents
                    List<BookSearchDocument> documents = batch.stream()
                        .map(this::mapEntityToDocument)
                        .collect(Collectors.toList());
                    
                    // Index batch
                    bookSearchRepository.indexBatch(documents);
                    indexedCount += documents.size();
                    
                    int progress = (int) ((endIndex * 100.0) / totalBooks);
                    log.info("Indexed batch {}-{} of {} ({}% complete)", 
                        i + 1, endIndex, totalBooks, progress);
                    
                } catch (Exception e) {
                    log.error("Failed to index batch {}-{}: {}", i + 1, endIndex, e.getMessage());
                    errorCount += batch.size();
                    
                    // Try to index individual books in the failed batch
                    for (AbstractBookEntity book : batch) {
                        if (indexBook(book)) {
                            indexedCount++;
                            errorCount--;
                        }
                    }
                }
            }
            
            long durationMs = System.currentTimeMillis() - startTime;
            log.info("Bulk indexing completed: {} indexed, {} errors, {}ms ({} books/sec)", 
                indexedCount, errorCount, durationMs, 
                durationMs > 0 ? (indexedCount * 1000 / durationMs) : 0);
            
            return new IndexingResult(totalBooks, indexedCount, errorCount, durationMs);
            
        } catch (Exception e) {
            log.error("Fatal error during bulk indexing: {}", e.getMessage(), e);
            long durationMs = System.currentTimeMillis() - startTime;
            return new IndexingResult(totalBooks, indexedCount, totalBooks - indexedCount, durationMs);
        }
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"searchResults", "searchFacets", "searchSuggestions", "frontPage"}, allEntries = true)
    public boolean updateBookIndex(Long bookId) {
        try {
            // Fetch book (lazy collections will be initialized in transaction)
            AbstractBookEntity book = abstractBookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));
            
            // Initialize all lazy collections needed for indexing within transaction
            initializeLazyCollections(book);
            
            return indexBook(book);
        } catch (Exception e) {
            log.error("Failed to update index for book ID {}: {}", bookId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    @CacheEvict(value = {"searchResults", "searchFacets", "searchSuggestions", "frontPage"}, allEntries = true)
    public boolean deleteBookFromIndex(Long bookId) {
        try {
            bookSearchRepository.deleteById(String.valueOf(bookId));
            log.debug("Successfully deleted book from index: {}", bookId);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete book {} from index: {}", bookId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Map AbstractBookEntity to BookSearchDocument
     */
    private BookSearchDocument mapEntityToDocument(AbstractBookEntity book) {
        BookSearchDocument document = new BookSearchDocument();
        
        // Basic fields
        document.setId(String.valueOf(book.getId()));
        document.setTitle(book.getTitle());
        document.setTitleText(book.getTitle()); // Same as title for Vietnamese text analysis
        document.setDescription(book.getDescription());
        document.setDescriptionText(book.getDescription()); // Same as description for Vietnamese text analysis
        
        // Authors
        if (book.getAuthors() != null) {
            List<String> authorNames = book.getAuthors().stream()
                .map(AuthorEntity::getName)
                .filter(name -> name != null && !name.isEmpty())
                .collect(Collectors.toList());
            document.setAuthorNames(authorNames);
        }
        
        // Genres
        if (book.getGenres() != null) {
            List<String> genreNames = book.getGenres().stream()
                .map(GenreEntity::getCode)
                .filter(code -> code != null && !code.isEmpty())
                .collect(Collectors.toList());
            document.setGenreNames(genreNames);
        }
        
        // Publisher
        if (book.getPublisher() != null) {
            document.setPublisherName(book.getPublisher().getName());
        }
        
        // Language
        if (book.getLanguage() != null) {
            document.setLanguage(book.getLanguage().name());
        }
        
        // Prices - get from related PhysicalBookEntity and EbookEntity
        setPricesFromAbstractBook(document, book);
        
        // Created timestamp
        if (book.getCreatedAt() != null) {
            document.setCreatedAt(Date.from(
                book.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()
            ));
        }
        
        // Rating and review count (set to null for now, can be populated later)
        document.setAverageRating(null);
        document.setReviewCount(null);
        
        return document;
    }
    
    /**
     * Initialize all lazy collections needed for indexing
     * This method triggers lazy loading within the transaction to avoid LazyInitializationException
     * 
     * @param book The book entity to initialize lazy collections for
     */
    private void initializeLazyCollections(AbstractBookEntity book) {
        // Initialize authors collection
        if (book.getAuthors() != null) {
            book.getAuthors().size(); // Trigger lazy loading
        }
        
        // Initialize genres collection
        if (book.getGenres() != null) {
            book.getGenres().size(); // Trigger lazy loading
        }
        
        // Initialize publisher (ManyToOne relationship)
        if (book.getPublisher() != null) {
            book.getPublisher().getName(); // Trigger lazy loading by accessing a property
        }
        
        // Note: translators and images are not needed for indexing, so we skip them
        // physicalBookInfo and ebookInfo are EAGER, so they're already loaded
    }
    
    /**
     * Set prices from AbstractBookEntity's related PhysicalBookEntity and EbookEntity
     */
    private void setPricesFromAbstractBook(BookSearchDocument document, AbstractBookEntity book) {
        // Get physical book price
        if (book.getPhysicalBookInfo() != null) {
            PhysicalBookEntity physicalBook = book.getPhysicalBookInfo();
            if (physicalBook.getCurrentPrice() != null) {
                document.setPhysicalPrice(physicalBook.getCurrentPrice().doubleValue());
            }
        }
        
        // Get ebook price
        if (book.getEbookInfo() != null) {
            EbookEntity ebook = book.getEbookInfo();
            if (ebook.getCurrentPrice() != null) {
                document.setDigitalPrice(ebook.getCurrentPrice().doubleValue());
            }
        }
    }
}

