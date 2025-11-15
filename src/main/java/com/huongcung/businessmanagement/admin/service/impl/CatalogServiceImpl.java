package com.huongcung.businessmanagement.admin.service.impl;

import com.huongcung.businessmanagement.admin.mapper.BookMapper;
import com.huongcung.businessmanagement.admin.model.BookCreateRequest;
import com.huongcung.businessmanagement.admin.model.BookDetailDTO;
import com.huongcung.businessmanagement.admin.model.BookImageData;
import com.huongcung.businessmanagement.admin.model.BookListDTO;
import com.huongcung.businessmanagement.admin.model.BookUpdateRequest;
import com.huongcung.businessmanagement.admin.service.CatalogService;
import com.huongcung.core.common.enumeration.Language;
import com.huongcung.core.contributor.model.entity.AuthorEntity;
import com.huongcung.core.contributor.model.entity.PublisherEntity;
import com.huongcung.core.contributor.model.entity.TranslatorEntity;
import com.huongcung.core.contributor.repository.AuthorRepository;
import com.huongcung.core.contributor.repository.PublisherRepository;
import com.huongcung.core.contributor.repository.TranslatorRepository;
import com.huongcung.core.media.model.entity.BookImageEntity;
import com.huongcung.core.media.repository.BookImageRepository;
import com.huongcung.core.media.service.ImageService;
import com.huongcung.core.product.model.entity.AbstractBookEntity;
import com.huongcung.core.product.model.entity.EbookEntity;
import com.huongcung.core.product.model.entity.GenreEntity;
import com.huongcung.core.product.model.entity.PhysicalBookEntity;
import com.huongcung.core.product.repository.AbstractBookRepository;
import com.huongcung.core.product.repository.GenreRepository;
import com.huongcung.core.search.model.dto.PaginationInfo;
import com.huongcung.core.search.service.SearchIndexService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.huongcung.core.media.constant.Constants.BOOKS_FOLDER;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogServiceImpl implements CatalogService {
    
    private final AbstractBookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final TranslatorRepository translatorRepository;
    private final GenreRepository genreRepository;
    private final BookMapper bookMapper;
    private final ImageService imageService;
    private final BookImageRepository bookImageRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    // Optional: SearchIndexService may not be available
    @Autowired(required = false)
    private SearchIndexService searchIndexService;
    
    @Override
    @Transactional(readOnly = true)
    public PaginatedBookResponse getAllBooks(Pageable pageable, String title, Language language, String bookType, Boolean isActive) {
        log.debug("Fetching books list - page: {}, size: {}, title: {}, language: {}, bookType: {}, isActive: {}", 
                pageable.getPageNumber(), pageable.getPageSize(), title, language, bookType, isActive);
        
        // Use Criteria API for dynamic filtering
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AbstractBookEntity> query = cb.createQuery(AbstractBookEntity.class);
        Root<AbstractBookEntity> root = query.from(AbstractBookEntity.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        if (title != null && !title.isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
        }
        
        if (language != null) {
            predicates.add(cb.equal(root.get("language"), language));
        }
        
        if (bookType != null && !bookType.isBlank()) {
            if ("PHYSICAL".equalsIgnoreCase(bookType)) {
                predicates.add(cb.equal(root.type(), PhysicalBookEntity.class));
            } else if ("EBOOK".equalsIgnoreCase(bookType)) {
                predicates.add(cb.equal(root.type(), EbookEntity.class));
            }
        }
        
        if (isActive != null) {
            predicates.add(cb.equal(root.get("isActive"), isActive));
        }
        
        query.where(predicates.toArray(new Predicate[0]));
        
        // Get total count
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        countQuery.select(cb.count(countQuery.from(AbstractBookEntity.class)));
        countQuery.where(predicates.toArray(new Predicate[0]));
        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();
        
        // Apply pagination
        List<AbstractBookEntity> books = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
        
        List<BookListDTO> bookDTOs = books.stream()
                .map(bookMapper::toListDTO)
                .collect(Collectors.toList());
        
        // Convert Spring Data Page (0-based) to PaginationInfo (1-based)
        PaginationInfo pagination = PaginationInfo.builder()
                .currentPage(pageable.getPageNumber() + 1) // Convert 0-based to 1-based
                .pageSize(pageable.getPageSize())
                .totalResults(totalCount)
                .totalPages((int) Math.ceil((double) totalCount / pageable.getPageSize()))
                .hasNext(pageable.getOffset() + pageable.getPageSize() < totalCount)
                .hasPrevious(pageable.getPageNumber() > 0)
                .build();
        
        log.debug("Found {} books (page {} of {})", totalCount, pagination.getCurrentPage(), pagination.getTotalPages());
        
        return new PaginatedBookResponse(bookDTOs, pagination);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BookDetailDTO getBookById(Long id) {
        log.debug("Fetching book by ID: {}", id);
        
        AbstractBookEntity book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + id));
        
        return bookMapper.toDetailDTO(book);
    }
    
    @Override
    @Transactional
    public BookDetailDTO createBook(BookCreateRequest request) {
        log.info("Creating book: title={}, bookType={}", request.getTitle(), request.getBookType());
        
        // Validate required fields
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        
        if (request.getLanguage() == null) {
            throw new IllegalArgumentException("Language is required");
        }
        
        if (request.getAuthorIds() == null || request.getAuthorIds().isEmpty()) {
            throw new IllegalArgumentException("At least one author is required");
        }
        
        if (request.getBookType() == null || request.getBookType().isBlank()) {
            throw new IllegalArgumentException("Book type is required (PHYSICAL or EBOOK)");
        }
        
        // TODO: Validate bookType
        if (!"PHYSICAL".equalsIgnoreCase(request.getBookType()) && !"EBOOK".equalsIgnoreCase(request.getBookType())) {
            throw new IllegalArgumentException("Book type must be either PHYSICAL or EBOOK");
        }
        
        // Generate unique book code
        String bookCode = generateBookCode(request.getTitle());
        
        // Load related entities
        List<AuthorEntity> authors = authorRepository.findByIdIn(request.getAuthorIds());
        if (authors.size() != request.getAuthorIds().size()) {
            throw new RuntimeException("One or more author IDs not found");
        }
        
        List<TranslatorEntity> translators = null;
        if (request.getTranslatorIds() != null && !request.getTranslatorIds().isEmpty()) {
            translators = translatorRepository.findByIdIn(request.getTranslatorIds());
            if (translators.size() != request.getTranslatorIds().size()) {
                throw new RuntimeException("One or more translator IDs not found");
            }
        }
        
        PublisherEntity publisher = null;
        if (request.getPublisherId() != null) {
            publisher = publisherRepository.findById(request.getPublisherId())
                    .orElseThrow(() -> new RuntimeException("Publisher not found with ID: " + request.getPublisherId()));
        }
        
        List<GenreEntity> genres = null;
        if (request.getGenreIds() != null && !request.getGenreIds().isEmpty()) {
            genres = genreRepository.findByIdIn(request.getGenreIds());
            if (genres.size() != request.getGenreIds().size()) {
                throw new RuntimeException("One or more genre IDs not found");
            }
        }
        
        // Create base book entity
        AbstractBookEntity book;
        
        if ("PHYSICAL".equalsIgnoreCase(request.getBookType())) { // TODO: Add enum
            PhysicalBookEntity physicalBook = new PhysicalBookEntity();
            physicalBook.setIsbn(request.getIsbn());
            physicalBook.setCoverType(request.getCoverType());
            physicalBook.setWeightGrams(request.getWeightGrams());
            physicalBook.setDimensions(request.getDimensions());
            physicalBook.setCurrentPrice(request.getCurrentPrice());
            book = physicalBook;
        } else {
            EbookEntity ebook = new EbookEntity();
            ebook.setFileUrl(request.getFileUrl());
            ebook.setFileName(request.getFileName());
            ebook.setFileSize(request.getFileSize());
            ebook.setFileFormat(request.getFileFormat());
            ebook.setDownloadCount(0);
            ebook.setCurrentPrice(request.getCurrentPrice());
            ebook.setIsActive(true);
            book = ebook;
        }
        
        // Set common fields
        book.setCode(bookCode);
        book.setTitle(request.getTitle());
        book.setDescription(request.getDescription());
        book.setLanguage(request.getLanguage());
        book.setPublicationDate(request.getPublicationDate());
        book.setPageCount(request.getPageCount());
        book.setEdition(request.getEdition());
        book.setAuthors(authors);
        book.setTranslators(translators);
        book.setPublisher(publisher);
        book.setGenres(genres);
        book.setHasPhysicalEdition(request.getHasPhysicalEdition() != null ? request.getHasPhysicalEdition() : false); // TODO: Delete
        book.setHasElectricEdition(request.getHasElectricEdition() != null ? request.getHasElectricEdition() : false); // TODO: Delete
        book.setIsActive(true);
        
        // Save book
        AbstractBookEntity savedBook = bookRepository.save(book);
        
        log.info("Book created successfully with ID: {}, code: {}", savedBook.getId(), savedBook.getCode());
        
        // Handle image uploads if provided
        if (!CollectionUtils.isEmpty(request.getImages())) {
            uploadBookImages(savedBook, request.getImages());
        }
        
        // Trigger search index update if service is available
        if (searchIndexService != null) {
            try {
                searchIndexService.indexBook(savedBook);
                log.debug("Book indexed in search service: {}", savedBook.getId());
            } catch (Exception e) {
                log.warn("Failed to index book in search service: {}", e.getMessage());
                // Don't fail the operation if indexing fails
            }
        }
        
        return bookMapper.toDetailDTO(savedBook);
    }
    
    @Override
    @Transactional
    public BookDetailDTO updateBook(Long id, BookUpdateRequest request, String updatedBy) {
        log.info("Updating book ID: {}, updatedBy: {}", id, updatedBy);
        
        AbstractBookEntity book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + id));
        
        // Track changes for audit logging
        StringBuilder changes = new StringBuilder();
        
        // Update relationships if provided
        if (request.getAuthorIds() != null) {
            List<AuthorEntity> authors = authorRepository.findByIdIn(request.getAuthorIds());
            if (authors.size() != request.getAuthorIds().size()) {
                throw new RuntimeException("One or more author IDs not found");
            }
            if (!Objects.equals(book.getAuthors(), authors)) {
                changes.append("authors updated; ");
                book.setAuthors(authors);
            }
        }
        
        if (request.getTranslatorIds() != null) {
            List<TranslatorEntity> translators = translatorRepository.findByIdIn(request.getTranslatorIds());
            if (translators.size() != request.getTranslatorIds().size()) {
                throw new RuntimeException("One or more translator IDs not found");
            }
            if (!Objects.equals(book.getTranslators(), translators)) {
                changes.append("translators updated; ");
                book.setTranslators(translators);
            }
        }
        
        if (request.getPublisherId() != null) {
            PublisherEntity publisher = publisherRepository.findById(request.getPublisherId())
                    .orElseThrow(() -> new RuntimeException("Publisher not found with ID: " + request.getPublisherId()));
            if (!Objects.equals(book.getPublisher() != null ? book.getPublisher().getId() : null, publisher.getId())) {
                changes.append("publisher updated; ");
                book.setPublisher(publisher);
            }
        }
        
        if (request.getGenreIds() != null) {
            List<GenreEntity> genres = genreRepository.findByIdIn(request.getGenreIds());
            if (genres.size() != request.getGenreIds().size()) {
                throw new RuntimeException("One or more genre IDs not found");
            }
            if (!Objects.equals(book.getGenres(), genres)) {
                changes.append("genres updated; ");
                book.setGenres(genres);
            }
        }
        
        // Update subtype-specific fields
        if (book instanceof PhysicalBookEntity physicalBook) {
            if (request.getIsbn() != null && !Objects.equals(physicalBook.getIsbn(), request.getIsbn())) {
                changes.append("isbn updated; ");
                physicalBook.setIsbn(request.getIsbn());
            }
            if (request.getCoverType() != null && !Objects.equals(physicalBook.getCoverType(), request.getCoverType())) {
                changes.append("coverType updated; ");
                physicalBook.setCoverType(request.getCoverType());
            }
            if (request.getWeightGrams() != null && !Objects.equals(physicalBook.getWeightGrams(), request.getWeightGrams())) {
                changes.append("weightGrams updated; ");
                physicalBook.setWeightGrams(request.getWeightGrams());
            }
            if (request.getDimensions() != null && !Objects.equals(physicalBook.getDimensions(), request.getDimensions())) {
                changes.append("dimensions updated; ");
                physicalBook.setDimensions(request.getDimensions());
            }
            if (request.getCurrentPrice() != null && !Objects.equals(physicalBook.getCurrentPrice(), request.getCurrentPrice())) {
                changes.append("currentPrice updated; ");
                physicalBook.setCurrentPrice(request.getCurrentPrice());
            }
        } else if (book instanceof EbookEntity ebook) {
            if (request.getFileUrl() != null && !Objects.equals(ebook.getFileUrl(), request.getFileUrl())) {
                changes.append("fileUrl updated; ");
                ebook.setFileUrl(request.getFileUrl());
            }
            if (request.getFileName() != null && !Objects.equals(ebook.getFileName(), request.getFileName())) {
                changes.append("fileName updated; ");
                ebook.setFileName(request.getFileName());
            }
            if (request.getFileSize() != null && !Objects.equals(ebook.getFileSize(), request.getFileSize())) {
                changes.append("fileSize updated; ");
                ebook.setFileSize(request.getFileSize());
            }
            if (request.getFileFormat() != null && !Objects.equals(ebook.getFileFormat(), request.getFileFormat())) {
                changes.append("fileFormat updated; ");
                ebook.setFileFormat(request.getFileFormat());
            }
            if (request.getCurrentPrice() != null && !Objects.equals(ebook.getCurrentPrice(), request.getCurrentPrice())) {
                changes.append("currentPrice updated; ");
                ebook.setCurrentPrice(request.getCurrentPrice());
            }
        }
        
        // Apply updates using mapper (handles common fields)
        bookMapper.updateEntityFromRequest(request, book);
        
        // Save updated entity
        AbstractBookEntity updatedBook = bookRepository.save(book);
        
        // Audit logging
        String changeLog = !changes.isEmpty() ? changes.toString() : "no changes";
        log.info("Book updated: bookId={}, updatedBy={}, changes={}, timestamp={}", 
                id, updatedBy, changeLog, LocalDateTime.now());
        
        // Trigger search index update if service is available
        if (searchIndexService != null) {
            try {
                searchIndexService.updateBookIndex(id);
                log.debug("Book index updated in search service: {}", id);
            } catch (Exception e) {
                log.warn("Failed to update book index in search service: {}", e.getMessage());
            }
        }
        
        return bookMapper.toDetailDTO(updatedBook);
    }

    @Override
    @Transactional
    public BookDetailDTO deactivateBook(Long id, String deactivatedBy) {
        log.info("Deactivating book ID: {}, deactivatedBy: {}", id, deactivatedBy);
        
        AbstractBookEntity book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + id));
        
        if (!book.getIsActive()) {
            log.warn("Book ID: {} is already deactivated", id);
        }
        
        book.setIsActive(false);
        
        // For EbookEntity, also set isActive flag
        if (book instanceof EbookEntity) {
            ((EbookEntity) book).setIsActive(false);
        }
        
        AbstractBookEntity deactivatedBook = bookRepository.save(book);
        
        // Audit logging
        log.info("Book deactivated: bookId={}, deactivatedBy={}, timestamp={}", 
                id, deactivatedBy, LocalDateTime.now());
        
        // Trigger search index removal if service is available
        if (searchIndexService != null) {
            try {
                searchIndexService.deleteBookFromIndex(id);
                log.debug("Book removed from search index: {}", id);
            } catch (Exception e) {
                log.warn("Failed to remove book from search index: {}", e.getMessage());
            }
        }
        
        return bookMapper.toDetailDTO(deactivatedBook);
    }

    @Override
    public void uploadBookImages(Long id, MultipartFile[] files) {
        log.info("Uploading {} images for book ID: {}", files.length, id);

        AbstractBookEntity book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + id));

        // Upload images using ImageService
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            if (file.isEmpty()) {
                continue;
            }

            try {
                // Get filename
                String fileName = file.getOriginalFilename();
                if (fileName == null || fileName.isBlank()) {
                    fileName = "image_" + (i + 1) + ".jpg";
                }

                // Get content type
                String contentType = file.getContentType();
                if (contentType == null || contentType.isBlank()) {
                    contentType = "image/jpeg";
                }

                // Save image to S3 with correct folder path
                String relativePath = imageService.saveImageFromStream(
                        file.getInputStream(),
                        fileName,
                        BOOKS_FOLDER,
                        contentType
                );

                // Create BookImageEntity
                BookImageEntity bookImage = new BookImageEntity();
                bookImage.setBook(book);
                bookImage.setUrl(relativePath);
                bookImage.setAltText("");
                bookImage.setPosition(i + 1); // Position starts from 1

                bookImageRepository.save(bookImage);

                log.debug("Image uploaded for book ID: {}, position: {}, url: {}", id, i + 1, relativePath);
            } catch (Exception e) {
                log.error("Failed to upload image for book ID: {}", id, e);
                throw new RuntimeException("Failed to upload image: " + e.getMessage());
            }
        }
    }

    /**
     * Generate unique book code from title
     * Format: BK-{first 3 uppercase letters of title}-{UUID first 8 chars}
     */
    private String generateBookCode(String title) {
        String prefix = title.length() >= 3 
                ? title.substring(0, 3).toUpperCase().replaceAll("[^A-Z0-9]", "")
                : "BK";
        if (prefix.isEmpty()) {
            prefix = "BK";
        }
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return prefix + "-" + suffix;
    }
    
    /**
     * Upload book images from Base64 data
     * @param book the book entity
     * @param images list of image data (Base64 encoded)
     */
    private void uploadBookImages(AbstractBookEntity book, List<BookImageData> images) {
        if (book == null || CollectionUtils.isEmpty(images)) {
            log.debug("No images to upload for book ID: {}", book != null ? book.getId() : "null");
            return;
        }

        for (int i = 0; i < images.size(); i++) {
            BookImageData imageData = images.get(i);
            
            if (imageData == null || !StringUtils.hasText(imageData.getBase64Data())) {
                log.warn("Skipping null image data at index {} for book ID: {}", i, book.getId());
                continue;
            }
            
            try {
                // Determine position (1 = cover, 2 = back cover, etc.)
                Integer position = imageData.getPosition();
                if (position == null || position <= 0) {
                    position = i + 1; // Default: sequential position starting from 1
                }
                
                // Generate filename if not provided
                String fileName = imageData.getFileName();
                if (fileName == null || fileName.isBlank()) {
                    fileName = "image_" + position + ".jpg"; // Default filename
                }

                // Upload image to S3
                String relativePath = imageService.saveImageFromBase64(
                    imageData.getBase64Data(),
                    fileName,
                    BOOKS_FOLDER
                );

                // Create BookImageEntity
                BookImageEntity bookImage = new BookImageEntity();
                bookImage.setBook(book);
                bookImage.setUrl(relativePath);
                bookImage.setAltText("");
                bookImage.setPosition(position);
                
                // Save to database
                BookImageEntity savedImage = bookImageRepository.save(bookImage);

                log.info("Image uploaded successfully for book ID: {}, imageId: {}, position: {}, url: {}",
                        book.getId(), savedImage.getId(), position, relativePath);
                
            } catch (Exception e) {
                log.error("Failed to upload image at index {} for book ID: {}", i, book.getId(), e);
                // Continue with other images even if one fails
                // In production, you might want to collect errors and report them
            }
        }
        
        log.info("Processed {} images for book ID: {}", images.size(), book.getId());
    }
}

