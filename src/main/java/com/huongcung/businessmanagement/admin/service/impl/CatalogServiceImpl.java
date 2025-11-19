package com.huongcung.businessmanagement.admin.service.impl;

import com.huongcung.businessmanagement.admin.mapper.BookMapper;
import com.huongcung.businessmanagement.admin.model.BookCreateRequest;
import com.huongcung.businessmanagement.admin.model.BookDetailDTO;
import com.huongcung.businessmanagement.admin.model.BookImageData;
import com.huongcung.businessmanagement.admin.model.BookListDTO;
import com.huongcung.businessmanagement.admin.model.BookUpdateRequest;
import com.huongcung.businessmanagement.admin.service.CatalogService;
import com.huongcung.core.catalog.model.entity.BookEntity;
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
import com.huongcung.core.catalog.model.entity.EbookEntity;
import com.huongcung.core.catalog.model.entity.GenreEntity;
import com.huongcung.core.catalog.model.entity.PhysicalBookEntity;
import com.huongcung.core.catalog.repository.AbstractBookRepository;
import com.huongcung.core.catalog.repository.GenreRepository;
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

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.huongcung.core.media.constant.FolderConstants.BOOKS;

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
        CriteriaQuery<BookEntity> query = cb.createQuery(BookEntity.class);
        Root<BookEntity> root = query.from(BookEntity.class);
        
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
        countQuery.select(cb.count(countQuery.from(BookEntity.class)));
        countQuery.where(predicates.toArray(new Predicate[0]));
        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();
        
        // Apply pagination
        List<BookEntity> books = entityManager.createQuery(query)
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
        
        BookEntity book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + id));
        
        return bookMapper.toDetailDTO(book);
    }
    
    @Override
    @Transactional
    public void createBook(BookCreateRequest request) {
        log.info("Creating book: title={}, physical={}, ebook={}",
                request.getTitle(), request.getHasPhysicalEdition(), request.getHasElectricEdition());
        
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

        List<BookEntity> savedBooks = new ArrayList<>();
        if (request.getHasPhysicalEdition()) {
            PhysicalBookEntity physicalBook = new PhysicalBookEntity();
            populate(request, physicalBook);
            physicalBook.setPublicationDate(request.getPublicationDate());
            physicalBook.setIsbn(request.getIsbn());
            physicalBook.setWeightGrams(request.getWeightGrams());
            physicalBook.setHeightCm(request.getHeightCm());
            physicalBook.setWidthCm(request.getWidthCm());
            physicalBook.setLengthCm(request.getLengthCm());
            physicalBook.setCurrentPrice(request.getPhysicalBookPrice());

            bookRepository.save(physicalBook);
            savedBooks.add(physicalBook);

            searchIndexService.indexBook(physicalBook);
            log.debug("Book indexed in search service: {}", physicalBook.getId());
        }

        if (request.getHasElectricEdition()) {
            EbookEntity ebook = new EbookEntity();
            populate(request, ebook);
            ebook.setPublicationDate(request.getPublicationDate());
            ebook.setCurrentPrice(request.getEbookPrice());

            bookRepository.save(ebook);
            savedBooks.add(ebook);

            searchIndexService.indexBook(ebook);
            log.debug("Book indexed in search service: {}", ebook.getId());
        }
        
        // Handle image uploads if provided
        if (!CollectionUtils.isEmpty(request.getImages())) {
            uploadBookImages(savedBooks, request.getImages());
        }

    }

    private void populate(BookCreateRequest source, BookEntity target) {
        // Generate unique book code
        String bookCode = generateBookCode(source.getTitle(), source.getEdition());

        // Load related entities
        List<AuthorEntity> authors = authorRepository.findByIdIn(source.getAuthorIds());
        if (authors.size() != source.getAuthorIds().size()) {
            throw new RuntimeException("One or more author IDs not found");
        }

        List<TranslatorEntity> translators = null;
        if (source.getTranslatorIds() != null && !source.getTranslatorIds().isEmpty()) {
            translators = translatorRepository.findByIdIn(source.getTranslatorIds());
            if (translators.size() != source.getTranslatorIds().size()) {
                throw new RuntimeException("One or more translator IDs not found");
            }
        }

        PublisherEntity publisher = null;
        if (source.getPublisherId() != null) {
            publisher = publisherRepository.findById(source.getPublisherId())
                    .orElseThrow(() -> new RuntimeException("Publisher not found with ID: " + source.getPublisherId()));
        }

        List<GenreEntity> genres = null;
        if (source.getGenreIds() != null && !source.getGenreIds().isEmpty()) {
            genres = genreRepository.findByIdIn(source.getGenreIds());
            if (genres.size() != source.getGenreIds().size()) {
                throw new RuntimeException("One or more genre IDs not found");
            }
        }

        target.setCode(bookCode);
        target.setTitle(source.getTitle());
        target.setDescription(source.getDescription());
        target.setLanguage(source.getLanguage());
        target.setPageCount(source.getPageCount());
        target.setEdition(source.getEdition());
        target.setAuthors(authors);
        target.setTranslators(translators);
        target.setPublisher(publisher);
        target.setGenres(genres);
        target.setIsActive(false);
    }

    @Override
    @Transactional
    public BookDetailDTO updateBook(Long id, BookUpdateRequest request, String updatedBy) {
        log.info("Updating book ID: {}, updatedBy: {}", id, updatedBy);
        
        BookEntity book = bookRepository.findById(id)
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
            if (request.getHeightCm() != null && !Objects.equals(physicalBook.getHeightCm(), request.getHeightCm())) {
                changes.append("heightCm updated; ");
                physicalBook.setHeightCm(request.getHeightCm());
            }
            if (request.getWidthCm() != null && !Objects.equals(physicalBook.getWidthCm(), request.getWidthCm())) {
                changes.append("widthCm updated; ");
                physicalBook.setWidthCm(request.getWidthCm());
            }
            if (request.getLengthCm() != null && !Objects.equals(physicalBook.getLengthCm(), request.getLengthCm())) {
                changes.append("lengthCm updated; ");
                physicalBook.setLengthCm(request.getLengthCm());
            }
            if (request.getCurrentPrice() != null && !Objects.equals(physicalBook.getCurrentPrice(), request.getCurrentPrice())) {
                changes.append("currentPrice updated; ");
                physicalBook.setCurrentPrice(request.getCurrentPrice());
            }
        } else if (book instanceof EbookEntity ebook) {
            // Update file information if provided
            if (request.getFileUrl() != null || request.getFileName() != null || request.getFileFormat() != null) {

            }
            
            if (request.getCurrentPrice() != null && !Objects.equals(ebook.getCurrentPrice(), request.getCurrentPrice())) {
                changes.append("currentPrice updated; ");
                ebook.setCurrentPrice(request.getCurrentPrice());
            }
        }
        
        // Apply updates using mapper (handles common fields)
        bookMapper.updateEntityFromRequest(request, book);
        
        // Save updated entity
        BookEntity updatedBook = bookRepository.save(book);
        
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
        
        BookEntity book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + id));
        
        if (!book.getIsActive()) {
            log.warn("Book ID: {} is already deactivated", id);
        }
        
        book.setIsActive(false);
        
        // For EbookEntity, also set isActive flag
        if (book instanceof EbookEntity) {
            ((EbookEntity) book).setIsActive(false);
        }
        
        BookEntity deactivatedBook = bookRepository.save(book);
        
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
//        log.info("Uploading {} images for book ID: {}", files.length, id);
//
//        AbstractBookEntity book = bookRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + id));
//
//        // Upload images using ImageService
//        for (int i = 0; i < files.length; i++) {
//            MultipartFile file = files[i];
//            if (file.isEmpty()) {
//                continue;
//            }
//
//            try {
//                // Get filename
//                String fileName = file.getOriginalFilename();
//                if (fileName == null || fileName.isBlank()) {
//                    fileName = "image_" + (i + 1) + ".jpg";
//                }
//
//                // Get content type
//                String contentType = file.getContentType();
//                if (contentType == null || contentType.isBlank()) {
//                    contentType = "image/jpeg";
//                }
//
//                // Save image to S3 with correct folder path
//                String relativePath = imageService.saveImageFromStream(
//                        file.getInputStream(),
//                        fileName,
//                        BOOKS,
//                        contentType
//                );
//
//                // Create BookImageEntity
//                BookImageEntity bookImage = new BookImageEntity();
//                bookImage.setBook(book);
//                bookImage.setUrl(relativePath);
//                bookImage.setAltText("");
//                bookImage.setPosition(i + 1); // Position starts from 1
//
//                bookImageRepository.save(bookImage);
//
//                log.debug("Image uploaded for book ID: {}, position: {}, url: {}", id, i + 1, relativePath);
//            } catch (Exception e) {
//                log.error("Failed to upload image for book ID: {}", id, e);
//                throw new RuntimeException("Failed to upload image: " + e.getMessage());
//            }
//        }
    }

    private String generateBookCode(String title, Integer edition) {

        // Normalize Vietnamese characters (remove diacritics)
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String withoutDiacritics = pattern.matcher(normalized).replaceAll("");

        return withoutDiacritics
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("\\s+", "-")  // Replace multiple spaces with single hyphen
                .replaceAll("[^a-z0-9-]", "")  // Remove special characters except hyphens
                .replaceAll("-+", "-")  // Replace multiple hyphens with single hyphen
                .replaceAll("^-|-$", "")
                .concat("-" + edition);
    }
    
    /**
     * Upload book images from Base64 data
     * @param book the book entity
     * @param images list of image data (Base64 encoded)
     */
    private void uploadBookImages(List<BookEntity> books, List<BookImageData> images) {
        if (CollectionUtils.isEmpty(books) || CollectionUtils.isEmpty(images)) {
            return;
        }

        for (int i = 0; i < images.size(); i++) {
            BookImageData imageData = images.get(i);
            
            if (imageData == null || !StringUtils.hasText(imageData.getBase64Data())) {
                log.warn("Skipping null image data at index {} for book: {}", i, books.get(0).getCode());
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
                    imageData.setFileName("image_" + position + ".jpg"); // Default filename
                }

                // Upload image to S3
                BookImageEntity savedImage = imageService.saveBookImageFromBase64(books, imageData, BOOKS);

                log.info("Image uploaded successfully for book: {}, imageId: {}, position: {}, url: {}",
                        books.get(0).getCode(), savedImage.getId(), position, savedImage.getUrl());
                
            } catch (Exception e) {
                log.error("Failed to upload image at index {} for book: {}", i, books.get(0).getCode(), e);
                // Continue with other images even if one fails
                // In production, you might want to collect errors and report them
            }
        }
        
        log.info("Processed {} images for book: {}", images.size(), books.get(0).getCode());
    }
}

