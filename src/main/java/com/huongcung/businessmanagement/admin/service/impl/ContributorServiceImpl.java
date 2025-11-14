package com.huongcung.businessmanagement.admin.service.impl;

import com.huongcung.businessmanagement.admin.mapper.ContributorMapper;
import com.huongcung.businessmanagement.admin.model.*;
import com.huongcung.businessmanagement.admin.service.ContributorService;
import com.huongcung.core.contributor.model.dto.AuthorDTO;
import com.huongcung.core.contributor.model.dto.PublisherDTO;
import com.huongcung.core.contributor.model.dto.TranslatorDTO;
import com.huongcung.core.contributor.model.entity.AuthorEntity;
import com.huongcung.core.contributor.model.entity.PublisherEntity;
import com.huongcung.core.contributor.model.entity.TranslatorEntity;
import com.huongcung.core.contributor.repository.AuthorRepository;
import com.huongcung.core.contributor.repository.PublisherRepository;
import com.huongcung.core.contributor.repository.TranslatorRepository;
import com.huongcung.core.product.model.entity.GenreEntity;
import com.huongcung.core.product.repository.GenreRepository;
import com.huongcung.core.search.model.dto.PaginationInfo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContributorServiceImpl implements ContributorService {
    
    private final AuthorRepository authorRepository;
    private final TranslatorRepository translatorRepository;
    private final PublisherRepository publisherRepository;
    private final GenreRepository genreRepository;
    private final ContributorMapper contributorMapper;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    // ========== Author Operations ==========
    
    @Override
    @Transactional(readOnly = true)
    public PaginatedAuthorResponse getAllAuthors(Pageable pageable, String name) {
        log.debug("Fetching authors list - page: {}, size: {}, name: {}", 
                pageable.getPageNumber(), pageable.getPageSize(), name);
        
        Page<AuthorEntity> page;
        if (name != null && !name.isBlank()) {
            page = authorRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            page = authorRepository.findAll(pageable);
        }
        
        List<AuthorListDTO> authors = page.getContent().stream()
                .map(contributorMapper::toListDTO)
                .collect(Collectors.toList());
        
        PaginationInfo pagination = PaginationInfo.builder()
                .currentPage(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalResults(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
        
        return new PaginatedAuthorResponse(authors, pagination);
    }
    
    @Override
    @Transactional(readOnly = true)
    public AuthorDTO getAuthorById(Long id) {
        log.debug("Fetching author by ID: {}", id);
        
        AuthorEntity author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with ID: " + id));
        
        return contributorMapper.toDetailDTO(author);
    }
    
    @Override
    @Transactional
    public AuthorDTO createAuthor(AuthorCreateRequest request) {
        log.info("Creating author: name={}", request.getName());
        
        AuthorEntity author = contributorMapper.toEntity(request);
        AuthorEntity savedAuthor = authorRepository.save(author);
        
        log.info("Author created successfully with ID: {}", savedAuthor.getId());
        
        return contributorMapper.toDetailDTO(savedAuthor);
    }
    
    @Override
    @Transactional
    public AuthorDTO updateAuthor(Long id, AuthorUpdateRequest request) {
        log.info("Updating author ID: {}", id);
        
        AuthorEntity author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with ID: " + id));
        
        contributorMapper.updateEntityFromRequest(request, author);
        AuthorEntity updatedAuthor = authorRepository.save(author);
        
        log.info("Author updated successfully: ID={}", id);
        
        return contributorMapper.toDetailDTO(updatedAuthor);
    }
    
    @Override
    @Transactional
    public void deleteAuthor(Long id) {
        log.info("Deleting author ID: {}", id);
        
        AuthorEntity author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with ID: " + id));
        
        // Check if author is referenced by any books
        if (isAuthorReferencedByBooks(id)) {
            throw new IllegalStateException(
                    "Cannot delete author: Author is referenced by one or more books. " +
                    "Please remove the author from all books before deleting.");
        }
        
        authorRepository.delete(author);
        log.info("Author deleted successfully: ID={}", id);
    }
    
    // ========== Translator Operations ==========
    
    @Override
    @Transactional(readOnly = true)
    public PaginatedTranslatorResponse getAllTranslators(Pageable pageable, String name) {
        log.debug("Fetching translators list - page: {}, size: {}, name: {}", 
                pageable.getPageNumber(), pageable.getPageSize(), name);
        
        Page<TranslatorEntity> page;
        if (name != null && !name.isBlank()) {
            page = translatorRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            page = translatorRepository.findAll(pageable);
        }
        
        List<TranslatorListDTO> translators = page.getContent().stream()
                .map(contributorMapper::toListDTO)
                .collect(Collectors.toList());
        
        PaginationInfo pagination = PaginationInfo.builder()
                .currentPage(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalResults(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
        
        return new PaginatedTranslatorResponse(translators, pagination);
    }
    
    @Override
    @Transactional(readOnly = true)
    public TranslatorDTO getTranslatorById(Long id) {
        log.debug("Fetching translator by ID: {}", id);
        
        TranslatorEntity translator = translatorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Translator not found with ID: " + id));
        
        return contributorMapper.toDetailDTO(translator);
    }
    
    @Override
    @Transactional
    public TranslatorDTO createTranslator(TranslatorCreateRequest request) {
        log.info("Creating translator: name={}", request.getName());
        
        TranslatorEntity translator = contributorMapper.toEntity(request);
        TranslatorEntity savedTranslator = translatorRepository.save(translator);
        
        log.info("Translator created successfully with ID: {}", savedTranslator.getId());
        
        return contributorMapper.toDetailDTO(savedTranslator);
    }
    
    @Override
    @Transactional
    public TranslatorDTO updateTranslator(Long id, TranslatorUpdateRequest request) {
        log.info("Updating translator ID: {}", id);
        
        TranslatorEntity translator = translatorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Translator not found with ID: " + id));
        
        contributorMapper.updateEntityFromRequest(request, translator);
        TranslatorEntity updatedTranslator = translatorRepository.save(translator);
        
        log.info("Translator updated successfully: ID={}", id);
        
        return contributorMapper.toDetailDTO(updatedTranslator);
    }
    
    @Override
    @Transactional
    public void deleteTranslator(Long id) {
        log.info("Deleting translator ID: {}", id);
        
        TranslatorEntity translator = translatorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Translator not found with ID: " + id));
        
        // Check if translator is referenced by any books
        if (isTranslatorReferencedByBooks(id)) {
            throw new IllegalStateException(
                    "Cannot delete translator: Translator is referenced by one or more books. " +
                    "Please remove the translator from all books before deleting.");
        }
        
        translatorRepository.delete(translator);
        log.info("Translator deleted successfully: ID={}", id);
    }
    
    // ========== Publisher Operations ==========
    
    @Override
    @Transactional(readOnly = true)
    public PaginatedPublisherResponse getAllPublishers(Pageable pageable, String name) {
        log.debug("Fetching publishers list - page: {}, size: {}, name: {}", 
                pageable.getPageNumber(), pageable.getPageSize(), name);
        
        Page<PublisherEntity> page;
        if (name != null && !name.isBlank()) {
            page = publisherRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            page = publisherRepository.findAll(pageable);
        }
        
        List<PublisherListDTO> publishers = page.getContent().stream()
                .map(contributorMapper::toListDTO)
                .collect(Collectors.toList());
        
        PaginationInfo pagination = PaginationInfo.builder()
                .currentPage(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalResults(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
        
        return new PaginatedPublisherResponse(publishers, pagination);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PublisherDTO getPublisherById(Long id) {
        log.debug("Fetching publisher by ID: {}", id);
        
        PublisherEntity publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publisher not found with ID: " + id));
        
        return contributorMapper.toDetailDTO(publisher);
    }
    
    @Override
    @Transactional
    public PublisherDTO createPublisher(PublisherCreateRequest request) {
        log.info("Creating publisher: name={}", request.getName());
        
        PublisherEntity publisher = contributorMapper.toEntity(request);
        PublisherEntity savedPublisher = publisherRepository.save(publisher);
        
        log.info("Publisher created successfully with ID: {}", savedPublisher.getId());
        
        return contributorMapper.toDetailDTO(savedPublisher);
    }
    
    @Override
    @Transactional
    public PublisherDTO updatePublisher(Long id, PublisherUpdateRequest request) {
        log.info("Updating publisher ID: {}", id);
        
        PublisherEntity publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publisher not found with ID: " + id));
        
        contributorMapper.updateEntityFromRequest(request, publisher);
        PublisherEntity updatedPublisher = publisherRepository.save(publisher);
        
        log.info("Publisher updated successfully: ID={}", id);
        
        return contributorMapper.toDetailDTO(updatedPublisher);
    }
    
    @Override
    @Transactional
    public void deletePublisher(Long id) {
        log.info("Deleting publisher ID: {}", id);
        
        PublisherEntity publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publisher not found with ID: " + id));
        
        // Check if publisher is referenced by any books
        if (isPublisherReferencedByBooks(id)) {
            throw new IllegalStateException(
                    "Cannot delete publisher: Publisher is referenced by one or more books. " +
                    "Please remove the publisher from all books before deleting.");
        }
        
        publisherRepository.delete(publisher);
        log.info("Publisher deleted successfully: ID={}", id);
    }
    
    // ========== Genre Operations ==========
    
    @Override
    @Transactional(readOnly = true)
    public PaginatedGenreResponse getAllGenres(Pageable pageable, String name, Long parentId, Boolean isActive) {
        log.debug("Fetching genres list - page: {}, size: {}, name: {}, parentId: {}, isActive: {}", 
                pageable.getPageNumber(), pageable.getPageSize(), name, parentId, isActive);
        
        // Use Criteria API or repository methods for filtering
        // For simplicity, using findAll and filtering in memory (can be optimized with custom query)
        Page<GenreEntity> page = genreRepository.findAll(pageable);
        
        List<GenreEntity> filtered = page.getContent().stream()
                .filter(genre -> name == null || name.isBlank() || genre.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(genre -> parentId == null || (genre.getParent() != null && genre.getParent().getId().equals(parentId)))
                .filter(genre -> isActive == null || genre.getIsActive().equals(isActive))
                .collect(Collectors.toList());
        
        List<GenreListDTO> genres = filtered.stream()
                .map(contributorMapper::toListDTO)
                .collect(Collectors.toList());
        
        // Note: Total count is approximate due to in-memory filtering
        PaginationInfo pagination = PaginationInfo.builder()
                .currentPage(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalResults((long) filtered.size())
                .totalPages((int) Math.ceil((double) filtered.size() / page.getSize()))
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
        
        return new PaginatedGenreResponse(genres, pagination);
    }
    
    @Override
    @Transactional(readOnly = true)
    public GenreListDTO getGenreById(Long id) {
        log.debug("Fetching genre by ID: {}", id);
        
        GenreEntity genre = genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre not found with ID: " + id));
        
        return contributorMapper.toListDTO(genre);
    }
    
    @Override
    @Transactional
    public GenreListDTO createGenre(GenreCreateRequest request) {
        log.info("Creating genre: name={}", request.getName());
        
        GenreEntity genre = contributorMapper.toEntity(request);
        
        // Set parent if provided
        if (request.getParentId() != null) {
            GenreEntity parent = genreRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent genre not found with ID: " + request.getParentId()));
            genre.setParent(parent);
        }
        
        genre.setIsActive(true); // Default to active
        
        GenreEntity savedGenre = genreRepository.save(genre);
        
        log.info("Genre created successfully with ID: {}", savedGenre.getId());
        
        return contributorMapper.toListDTO(savedGenre);
    }
    
    @Override
    @Transactional
    public GenreListDTO updateGenre(Long id, GenreUpdateRequest request) {
        log.info("Updating genre ID: {}", id);
        
        GenreEntity genre = genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre not found with ID: " + id));
        
        contributorMapper.updateEntityFromRequest(request, genre);
        
        // Update parent if provided
        if (request.getParentId() != null) {
            GenreEntity parent = genreRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent genre not found with ID: " + request.getParentId()));
            genre.setParent(parent);
        } else if (request.getParentId() != null && request.getParentId() == 0) {
            // Explicitly remove parent
            genre.setParent(null);
        }
        
        // Update isActive if provided
        if (request.getIsActive() != null) {
            genre.setIsActive(request.getIsActive());
        }
        
        GenreEntity updatedGenre = genreRepository.save(genre);
        
        log.info("Genre updated successfully: ID={}", id);
        
        return contributorMapper.toListDTO(updatedGenre);
    }
    
    @Override
    @Transactional
    public void deleteGenre(Long id) {
        log.info("Deleting genre ID: {}", id);
        
        GenreEntity genre = genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre not found with ID: " + id));
        
        // Check if genre is referenced by any books
        if (isGenreReferencedByBooks(id)) {
            throw new IllegalStateException(
                    "Cannot delete genre: Genre is referenced by one or more books. " +
                    "Please remove the genre from all books before deleting.");
        }
        
        // Check if genre has children
        if (genre.getChildren() != null && !genre.getChildren().isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete genre: Genre has child genres. " +
                    "Please delete or reassign child genres before deleting this genre.");
        }
        
        genreRepository.delete(genre);
        log.info("Genre deleted successfully: ID={}", id);
    }
    
    // ========== Deletion Validation Helper Methods ==========
    
    private boolean isAuthorReferencedByBooks(Long authorId) {
        Query query = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM books_authors WHERE author_id = :authorId");
        query.setParameter("authorId", authorId);
        Long count = ((Number) query.getSingleResult()).longValue();
        return count > 0;
    }
    
    private boolean isTranslatorReferencedByBooks(Long translatorId) {
        Query query = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM books_translators WHERE translator_id = :translatorId");
        query.setParameter("translatorId", translatorId);
        Long count = ((Number) query.getSingleResult()).longValue();
        return count > 0;
    }
    
    private boolean isPublisherReferencedByBooks(Long publisherId) {
        Query query = entityManager.createQuery(
                "SELECT COUNT(b) FROM AbstractBookEntity b WHERE b.publisher.id = :publisherId");
        query.setParameter("publisherId", publisherId);
        Long count = (Long) query.getSingleResult();
        return count > 0;
    }
    
    private boolean isGenreReferencedByBooks(Long genreId) {
        Query query = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM books_genres WHERE genre_id = :genreId");
        query.setParameter("genreId", genreId);
        Long count = ((Number) query.getSingleResult()).longValue();
        return count > 0;
    }
}

