package com.huongcung.businessmanagement.admin.service.impl;

import com.huongcung.businessmanagement.admin.mapper.AdminBookMapper;
import com.huongcung.businessmanagement.admin.model.BookCreateRequest;
import com.huongcung.businessmanagement.admin.model.BookDetailDTO;
import com.huongcung.businessmanagement.admin.model.BookImageData;
import com.huongcung.businessmanagement.admin.model.BookListDTO;
import com.huongcung.businessmanagement.admin.model.BookUpdateRequest;
import com.huongcung.businessmanagement.admin.service.CatalogService;
import com.huongcung.core.catalog.model.entity.AbstractBookEntity;
import com.huongcung.core.catalog.repository.EbookRepository;
import com.huongcung.core.catalog.repository.PhysicalBookRepository;
import com.huongcung.core.common.enumeration.Language;
import com.huongcung.core.contributor.model.entity.AuthorEntity;
import com.huongcung.core.contributor.model.entity.PublisherEntity;
import com.huongcung.core.contributor.model.entity.TranslatorEntity;
import com.huongcung.core.contributor.repository.AuthorRepository;
import com.huongcung.core.contributor.repository.PublisherRepository;
import com.huongcung.core.contributor.repository.TranslatorRepository;
import com.huongcung.core.media.model.entity.BookImageEntity;
import com.huongcung.core.media.repository.BookImageEntityRepository;
import com.huongcung.core.media.enumeration.FileType;
import com.huongcung.core.storage.service.StorageService;
import com.huongcung.core.catalog.model.entity.EbookEntity;
import com.huongcung.core.contributor.model.entity.GenreEntity;
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
import static com.huongcung.core.media.constant.FolderConstants.IMAGES;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogServiceImpl implements CatalogService {
    
    private final AbstractBookRepository abstractBookRepository;
    private final PhysicalBookRepository physicalBookRepository;
    private final EbookRepository ebookRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final TranslatorRepository translatorRepository;
    private final GenreRepository genreRepository;
    private final AdminBookMapper bookMapper;
    private final BookImageEntityRepository bookImageEntityRepository;
    private final StorageService storageService;
    
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
        
        // Use Criteria API for dynamic filtering with AbstractBookEntity
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
                predicates.add(cb.isNotNull(root.get("physicalBookInfo")));
            } else if ("EBOOK".equalsIgnoreCase(bookType)) {
                predicates.add(cb.isNotNull(root.get("ebookInfo")));
            }
        }
        
        // Note: AbstractBookEntity doesn't have isActive field, filter through related entities if needed
        // For now, we'll filter through the related PhysicalBookEntity or EbookEntity if needed
        
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
        
        // Convert AbstractBookEntity to BookListDTO
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
    
    private BookListDTO mapAbstractBookToListDTO(AbstractBookEntity abstractBook) {
        // Helper method to map AbstractBookEntity to BookListDTO
        BookListDTO dto = new BookListDTO();
        dto.setId(abstractBook.getId());
        dto.setCode(abstractBook.getCode());
        dto.setTitle(abstractBook.getTitle());
        dto.setLanguage(abstractBook.getLanguage());
        // Determine book type
        if (abstractBook.getPhysicalBookInfo() != null) {
            dto.setBookType("PHYSICAL");
        } else if (abstractBook.getEbookInfo() != null) {
            dto.setBookType("EBOOK");
        }
        return dto;
    }
    
    @Override
    @Transactional(readOnly = true)
    public BookDetailDTO getBookById(Long id) {
        log.debug("Fetching book by ID: {}", id);
        
        // Get AbstractBookEntity
        AbstractBookEntity abstractBook = abstractBookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + id));
        
        return bookMapper.toDetailDTO(abstractBook);
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

        // Create AbstractBookEntity first (main entity with relationships)
        AbstractBookEntity abstractBook = new AbstractBookEntity();
        populateAbstractBook(request, abstractBook);
        
        // Save AbstractBookEntity first
        AbstractBookEntity savedAbstractBook = abstractBookRepository.save(abstractBook);
        
        List<AbstractBookEntity> savedAbstractBooks = new ArrayList<>();
        savedAbstractBooks.add(savedAbstractBook);

        // Create PhysicalBookEntity if requested
        if (request.getHasPhysicalEdition()) {
            PhysicalBookEntity physicalBook = new PhysicalBookEntity();
            
            // Set PhysicalBookEntity specific fields
            physicalBook.setPublicationDate(request.getPublicationDate());
            physicalBook.setIsbn(request.getIsbn());
            physicalBook.setWeightGrams(request.getWeightGrams());
            physicalBook.setHeightCm(request.getHeightCm());
            physicalBook.setWidthCm(request.getWidthCm());
            physicalBook.setLengthCm(request.getLengthCm());
            physicalBook.setCurrentPrice(request.getPhysicalBookPrice());
            physicalBook.setCoverType(request.getCoverType());
            
            // Link to AbstractBookEntity
            physicalBook.setAbstractBook(savedAbstractBook);
            savedAbstractBook.setPhysicalBookInfo(physicalBook);

            physicalBookRepository.save(physicalBook);
        }

        // Create EbookEntity if requested
        if (request.getHasElectricEdition()) {
            EbookEntity ebook = new EbookEntity();
            ebook.setIsActive(false);
            
            // Set EbookEntity specific fields
            ebook.setPublicationDate(request.getPublicationDate());
            ebook.setCurrentPrice(request.getEbookPrice());
            if (request.getEisbn() != null) {
                ebook.setIsbn(request.getEisbn());
            }

            // Link to AbstractBookEntity
            ebook.setAbstractBook(savedAbstractBook);
            savedAbstractBook.setEbookInfo(ebook);

            // Save via AbstractBookEntity (cascade will save EbookEntity)
            ebookRepository.save(ebook);
        }

        if (searchIndexService != null) {
            searchIndexService.indexBook(savedAbstractBook);
            log.debug("Book indexed in search service: {}", savedAbstractBook.getId());
        }
        
        // Handle image uploads if provided
        if (!CollectionUtils.isEmpty(request.getImages())) {
            uploadBookImagesToAbstractBook(savedAbstractBooks, request.getImages());
        }

    }

    private void populateAbstractBook(BookCreateRequest source, AbstractBookEntity target) {
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
    }

    @Override
    @Transactional
    public BookDetailDTO updateBook(Long id, BookUpdateRequest request, String updatedBy) {
        log.info("Updating book ID: {}, updatedBy: {}", id, updatedBy);
        
        // Get AbstractBookEntity
        AbstractBookEntity abstractBook = abstractBookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + id));
        
        // Track changes for audit logging
        StringBuilder changes = new StringBuilder();
        
        // Update AbstractBookEntity relationships
        // Update relationships if provided
        if (request.getAuthorIds() != null) {
            List<AuthorEntity> authors = authorRepository.findByIdIn(request.getAuthorIds());
            if (authors.size() != request.getAuthorIds().size()) {
                throw new RuntimeException("One or more author IDs not found");
            }
            if (!Objects.equals(abstractBook.getAuthors(), authors)) {
                changes.append("authors updated; ");
                abstractBook.setAuthors(authors);
            }
        }
        
        if (request.getTranslatorIds() != null) {
            List<TranslatorEntity> translators = translatorRepository.findByIdIn(request.getTranslatorIds());
            if (translators.size() != request.getTranslatorIds().size()) {
                throw new RuntimeException("One or more translator IDs not found");
            }
            if (!Objects.equals(abstractBook.getTranslators(), translators)) {
                changes.append("translators updated; ");
                abstractBook.setTranslators(translators);
            }
        }
        
        if (request.getPublisherId() != null) {
            PublisherEntity publisher = publisherRepository.findById(request.getPublisherId())
                    .orElseThrow(() -> new RuntimeException("Publisher not found with ID: " + request.getPublisherId()));
            if (!Objects.equals(abstractBook.getPublisher() != null ? abstractBook.getPublisher().getId() : null, publisher.getId())) {
                changes.append("publisher updated; ");
                abstractBook.setPublisher(publisher);
            }
        }
        
        if (request.getGenreIds() != null) {
            List<GenreEntity> genres = genreRepository.findByIdIn(request.getGenreIds());
            if (genres.size() != request.getGenreIds().size()) {
                throw new RuntimeException("One or more genre IDs not found");
            }
            if (!Objects.equals(abstractBook.getGenres(), genres)) {
                changes.append("genres updated; ");
                abstractBook.setGenres(genres);
            }
        }
        
        // Update common fields in AbstractBookEntity
        if (request.getTitle() != null && !Objects.equals(abstractBook.getTitle(), request.getTitle())) {
            changes.append("title updated; ");
            abstractBook.setTitle(request.getTitle());
        }
        if (request.getDescription() != null && !Objects.equals(abstractBook.getDescription(), request.getDescription())) {
            changes.append("description updated; ");
            abstractBook.setDescription(request.getDescription());
        }
        if (request.getLanguage() != null && !Objects.equals(abstractBook.getLanguage(), request.getLanguage())) {
            changes.append("language updated; ");
            abstractBook.setLanguage(request.getLanguage());
        }
        if (request.getPageCount() != null && !Objects.equals(abstractBook.getPageCount(), request.getPageCount())) {
            changes.append("pageCount updated; ");
            abstractBook.setPageCount(request.getPageCount());
        }
        if (request.getEdition() != null && !Objects.equals(abstractBook.getEdition(), request.getEdition())) {
            changes.append("edition updated; ");
            abstractBook.setEdition(request.getEdition());
        }
        
        // Update subtype-specific fields
        if (abstractBook.getPhysicalBookInfo() != null) {
            PhysicalBookEntity physicalBook = abstractBook.getPhysicalBookInfo();
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
            if (request.getPublicationDate() != null && !Objects.equals(physicalBook.getPublicationDate(), request.getPublicationDate())) {
                changes.append("publicationDate updated; ");
                physicalBook.setPublicationDate(request.getPublicationDate());
            }
            if (request.getIsActive() != null && !Objects.equals(physicalBook.getIsAvailable(), request.getIsActive())) {
                changes.append("isAvailable updated; ");
                physicalBook.setIsAvailable(request.getIsActive());
            }
        } else if (abstractBook.getEbookInfo() != null) {
            EbookEntity ebook = abstractBook.getEbookInfo();
            if (request.getCurrentPrice() != null && !Objects.equals(ebook.getCurrentPrice(), request.getCurrentPrice())) {
                changes.append("currentPrice updated; ");
                ebook.setCurrentPrice(request.getCurrentPrice());
            }
            if (request.getPublicationDate() != null && !Objects.equals(ebook.getPublicationDate(), request.getPublicationDate())) {
                changes.append("publicationDate updated; ");
                ebook.setPublicationDate(request.getPublicationDate());
            }
            if (request.getIsActive() != null && !Objects.equals(ebook.getIsActive(), request.getIsActive())) {
                changes.append("isActive updated; ");
                ebook.setIsActive(request.getIsActive());
            }
        }
        
        // Apply updates using mapper (handles common fields in AbstractBookEntity)
        bookMapper.updateEntityFromRequest(request, abstractBook);
        
        // Save updated entities
        AbstractBookEntity updatedBook = abstractBookRepository.save(abstractBook);
        
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
        
        return bookMapper.toDetailDTO(abstractBook);
    }

    @Override
    @Transactional
    public BookDetailDTO deactivateBook(Long id, String deactivatedBy) {
        log.info("Deactivating book ID: {}, deactivatedBy: {}", id, deactivatedBy);
        
        // Get AbstractBookEntity
        AbstractBookEntity abstractBook = abstractBookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + id));
        
        // Deactivate PhysicalBookEntity if exists
        if (abstractBook.getPhysicalBookInfo() != null) {
            PhysicalBookEntity physicalBook = abstractBook.getPhysicalBookInfo();
            if (Boolean.TRUE.equals(physicalBook.getIsAvailable())) {
                physicalBook.setIsAvailable(false);
            } else {
                log.warn("Physical book ID: {} is already deactivated", id);
            }
        }
        
        // Deactivate EbookEntity if exists
        if (abstractBook.getEbookInfo() != null) {
            EbookEntity ebook = abstractBook.getEbookInfo();
            if (Boolean.TRUE.equals(ebook.getIsActive())) {
                ebook.setIsActive(false);
            } else {
                log.warn("Ebook ID: {} is already deactivated", id);
            }
        }
        
        // Save via AbstractBookEntity (cascade will save related entities)
        abstractBookRepository.save(abstractBook);
        
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
        
        return bookMapper.toDetailDTO(abstractBook);
    }

    @Override
    public void uploadBookImages(Long id, MultipartFile[] files) {
        log.info("Uploading {} images for book ID: {}", files.length, id);

        // Try to find as AbstractBookEntity first
        AbstractBookEntity abstractBook = abstractBookRepository.findById(id)
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
                    fileName = "image_" + abstractBook.getCode() + "_" + (i + 1) + ".jpg";
                }

                // Get content type
                String contentType = file.getContentType();
                if (contentType == null || contentType.isBlank()) {
                    contentType = "image/jpeg";
                }

                // Save image to S3 with correct folder path
                String folderPath = IMAGES + "/" + BOOKS;
                String relativePath = storageService.save(
                        file.getInputStream(),
                        fileName,
                        folderPath,
                        contentType
                );

                // Create BookImageEntityv2
                BookImageEntity bookImage = new BookImageEntity();
                bookImage.setBook(abstractBook);
                bookImage.setUrl(relativePath);
                bookImage.setAltText(fileName);
                bookImage.setFileName(fileName);
                bookImage.setFileType(FileType.findFileTypeByCode(contentType));
                bookImage.setPosition(i + 1); // Position starts from 1

                bookImageEntityRepository.save(bookImage);

                // Add to abstractBook's images list
                if (abstractBook.getImages() == null) {
                    abstractBook.setImages(new ArrayList<>());
                }
                abstractBook.getImages().add(bookImage);
                abstractBookRepository.save(abstractBook);

                log.debug("Image uploaded for book ID: {}, position: {}, url: {}", id, i + 1, relativePath);
            } catch (Exception e) {
                log.error("Failed to upload image for book ID: {}", id, e);
                throw new RuntimeException("Failed to upload image: " + e.getMessage());
            }
        }
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
     * Upload book images from Base64 data to AbstractBookEntity using BookImageEntityv2
     * @param abstractBooks list of AbstractBookEntity
     * @param images list of image data (Base64 encoded)
     */
    private void uploadBookImagesToAbstractBook(List<AbstractBookEntity> abstractBooks, List<BookImageData> images) {
        if (CollectionUtils.isEmpty(abstractBooks) || CollectionUtils.isEmpty(images)) {
            return;
        }

        AbstractBookEntity abstractBook = abstractBooks.get(0);

        for (int i = 0; i < images.size(); i++) {
            BookImageData imageData = images.get(i);
            
            if (imageData == null || !StringUtils.hasText(imageData.getBase64Data())) {
                log.warn("Skipping null image data at index {} for book: {}", i, abstractBook.getCode());
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
                    fileName = "image_" + abstractBook.getCode() + "_" + position + ".jpg"; // Default filename
                }

                // Save image to S3 using StorageService
                String folderPath = IMAGES + "/" + BOOKS;
                String contentType = imageData.getFileType() != null ? imageData.getFileType() : "image/jpeg";
                String relativePath = storageService.save(
                        imageData.getBase64Data(),
                        fileName,
                        folderPath,
                        contentType
                );

                // Create BookImageEntityv2
                BookImageEntity bookImage = new BookImageEntity();
                bookImage.setBook(abstractBook);
                bookImage.setUrl(relativePath);
                bookImage.setAltText(fileName);
                bookImage.setFileName(fileName);
                bookImage.setFileType(FileType.findFileTypeByCode(contentType));
                bookImage.setPosition(position);

                // Save the image entity
                BookImageEntity savedImage = bookImageEntityRepository.save(bookImage);
                
                // Add to abstractBook's images list if not already there
                if (abstractBook.getImages() == null) {
                    abstractBook.setImages(new ArrayList<>());
                }
                if (!abstractBook.getImages().contains(savedImage)) {
                    abstractBook.getImages().add(savedImage);
                }
                abstractBookRepository.save(abstractBook);

                log.info("Image uploaded successfully for book: {}, imageId: {}, position: {}, url: {}",
                        abstractBook.getCode(), savedImage.getId(), position, relativePath);
                
            } catch (Exception e) {
                log.error("Failed to upload image at index {} for book: {}", i, abstractBook.getCode(), e);
                // Continue with other images even if one fails
            }
        }
        
        log.info("Processed {} images for book: {}", images.size(), abstractBook.getCode());
    }
}

