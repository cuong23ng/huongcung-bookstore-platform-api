package com.huongcung.core.search.service.impl;

import com.huongcung.core.common.enumeration.Language;
import com.huongcung.core.contributor.model.entity.AuthorEntity;
import com.huongcung.core.contributor.model.entity.PublisherEntity;
import com.huongcung.core.inventory.enumeration.City;
import com.huongcung.core.product.model.entity.AbstractBookEntity;
import com.huongcung.core.product.model.entity.EbookEntity;
import com.huongcung.core.product.model.entity.GenreEntity;
import com.huongcung.core.product.model.entity.PhysicalBookEntity;
import com.huongcung.core.product.repository.AbstractBookRepository;
import com.huongcung.core.search.model.entity.BookSearchDocument;
import com.huongcung.core.search.repository.BookSearchRepository;
import com.huongcung.core.search.service.SearchIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
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
    @CacheEvict(value = {"searchResults", "searchFacets", "searchSuggestions"}, allEntries = true)
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
    public IndexingResult indexAllBooks() {
        long startTime = System.currentTimeMillis();
        long totalBooks = 0;
        long indexedCount = 0;
        long errorCount = 0;
        
        try {
            log.info("Starting bulk indexing of all books...");
            
            // Fetch all books from repository
            List<AbstractBookEntity> allBooks = abstractBookRepository.findAll();
            totalBooks = allBooks.size();
            
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
    @CacheEvict(value = {"searchResults", "searchFacets", "searchSuggestions"}, allEntries = true)
    public boolean updateBookIndex(Long bookId) {
        try {
            AbstractBookEntity book = abstractBookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));
            
            return indexBook(book);
        } catch (Exception e) {
            log.error("Failed to update index for book ID {}: {}", bookId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    @CacheEvict(value = {"searchResults", "searchFacets", "searchSuggestions"}, allEntries = true)
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
        
        // ISBN (only for physical books)
        if (book instanceof PhysicalBookEntity) {
            PhysicalBookEntity physicalBook = (PhysicalBookEntity) book;
            document.setIsbn(physicalBook.getIsbn());
        }
        
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
                .map(GenreEntity::getName)
                .filter(name -> name != null && !name.isEmpty())
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
        
        // Format (PHYSICAL, DIGITAL, or BOTH)
        String format = determineFormat(book);
        document.setFormat(format);
        
        // Prices
        setPrices(document, book);
        
        // Publication date
        if (book.getPublicationDate() != null) {
            Date publicationDate = Date.from(
                book.getPublicationDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
            );
            document.setPublicationDate(publicationDate);
        }
        
        // City availability
        setCityAvailability(document, book);
        
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
     * Determine book format based on edition flags
     */
    private String determineFormat(AbstractBookEntity book) {
        boolean hasPhysical = book.isHasPhysicalEdition();
        boolean hasDigital = book.isHasElectricEdition();
        
        if (hasPhysical && hasDigital) {
            return "BOTH";
        } else if (hasPhysical) {
            return "PHYSICAL";
        } else if (hasDigital) {
            return "DIGITAL";
        } else {
            return "PHYSICAL"; // Default fallback
        }
    }
    
    /**
     * Set prices from PhysicalBookEntity and EbookEntity
     */
    private void setPrices(BookSearchDocument document, AbstractBookEntity book) {
        if (book instanceof PhysicalBookEntity) {
            PhysicalBookEntity physicalBook = (PhysicalBookEntity) book;
            if (physicalBook.getCurrentPrice() != null) {
                document.setPhysicalPrice(physicalBook.getCurrentPrice().doubleValue());
            }
        }
        
        if (book instanceof EbookEntity) {
            EbookEntity ebook = (EbookEntity) book;
            if (ebook.getCurrentPrice() != null) {
                document.setDigitalPrice(ebook.getCurrentPrice().doubleValue());
            }
        }
        
        // Handle books that have both editions
        // If book has both flags but is only one entity type, check if we need to query the other
        // For now, we'll rely on the entity type to determine which price to set
    }
    
    /**
     * Set city availability flags
     * For now, we set all to false as a safe default
     * This can be enhanced later by querying StockLevelEntity repository
     */
    private void setCityAvailability(BookSearchDocument document, AbstractBookEntity book) {
        // Default to false for all cities
        // TODO: Enhance this to query StockLevelEntity repository to check actual availability
        // For now, we'll set based on whether it's a physical book
        boolean isPhysical = book.isHasPhysicalEdition();
        
        // If it's a physical book, we assume it might be available (set to true)
        // Otherwise, set to false
        // This is a simplified approach - in production, query actual stock levels
        document.setAvailableInHanoi(isPhysical);
        document.setAvailableInHcmc(isPhysical);
        document.setAvailableInDanang(isPhysical);
    }
}

