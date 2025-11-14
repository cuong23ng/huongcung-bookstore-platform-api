package com.huongcung.businessmanagement.admin.service;

import com.huongcung.businessmanagement.admin.model.*;
import com.huongcung.core.contributor.model.dto.AuthorDTO;
import com.huongcung.core.contributor.model.dto.PublisherDTO;
import com.huongcung.core.contributor.model.dto.TranslatorDTO;
import com.huongcung.core.search.model.dto.PaginationInfo;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for contributor management operations
 * Handles CRUD operations for Authors, Translators, Publishers, and Genres
 */
public interface ContributorService {
    
    // ========== Author Operations ==========
    
    PaginatedAuthorResponse getAllAuthors(Pageable pageable, String name);
    AuthorDTO getAuthorById(Long id);
    AuthorDTO createAuthor(AuthorCreateRequest request);
    AuthorDTO updateAuthor(Long id, AuthorUpdateRequest request);
    void deleteAuthor(Long id);
    
    // ========== Translator Operations ==========
    
    PaginatedTranslatorResponse getAllTranslators(Pageable pageable, String name);
    TranslatorDTO getTranslatorById(Long id);
    TranslatorDTO createTranslator(TranslatorCreateRequest request);
    TranslatorDTO updateTranslator(Long id, TranslatorUpdateRequest request);
    void deleteTranslator(Long id);
    
    // ========== Publisher Operations ==========
    
    PaginatedPublisherResponse getAllPublishers(Pageable pageable, String name);
    PublisherDTO getPublisherById(Long id);
    PublisherDTO createPublisher(PublisherCreateRequest request);
    PublisherDTO updatePublisher(Long id, PublisherUpdateRequest request);
    void deletePublisher(Long id);
    
    // ========== Genre Operations ==========
    
    PaginatedGenreResponse getAllGenres(Pageable pageable, String name, Long parentId, Boolean isActive);
    GenreListDTO getGenreById(Long id);
    GenreListDTO createGenre(GenreCreateRequest request);
    GenreListDTO updateGenre(Long id, GenreUpdateRequest request);
    void deleteGenre(Long id);
    
    // ========== Response Records ==========
    
    record PaginatedAuthorResponse(java.util.List<AuthorListDTO> authors, PaginationInfo pagination) {}
    record PaginatedTranslatorResponse(java.util.List<TranslatorListDTO> translators, PaginationInfo pagination) {}
    record PaginatedPublisherResponse(java.util.List<PublisherListDTO> publishers, PaginationInfo pagination) {}
    record PaginatedGenreResponse(java.util.List<GenreListDTO> genres, PaginationInfo pagination) {}
}

