package com.huongcung.core.search.repository.impl;

import com.huongcung.core.search.config.SolrConfig;
import com.huongcung.core.search.model.entity.BookSearchDocument;
import com.huongcung.core.search.repository.BookSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SuggesterResponse;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementation of BookSearchRepository using SolrJ client
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class BookSearchRepositoryImpl implements BookSearchRepository {
    
    private final SolrConfig solrConfig;
    private SolrClient solrClient;
    
    /**
     * Get or create Solr client instance
     * Lazy initialization to handle Solr unavailability gracefully
     */
    private SolrClient getSolrClient() {
        if (solrClient == null) {
            try {
                String baseUrl = solrConfig.getBaseUrl();
                solrClient = new Http2SolrClient.Builder(baseUrl)
                    .withConnectionTimeout(solrConfig.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                    .build();
                log.info("Solr client initialized for base URL: {}", baseUrl);
            } catch (Exception e) {
                log.error("Failed to initialize Solr client: {}", e.getMessage());
                throw new RuntimeException("Solr client initialization failed", e);
            }
        }
        return solrClient;
    }
    
    @Override
    public QueryResponse search(String query, int start, int rows) {
        try {
            SolrQuery solrQuery = new SolrQuery(query);
            solrQuery.setStart(start);
            solrQuery.setRows(rows);
            return getSolrClient().query(solrConfig.getCore(), solrQuery);
        } catch (SolrServerException | IOException | RuntimeException e) {
            log.error("Solr search failed: {}", e.getMessage());
            throw new RuntimeException("Solr search failed", e);
        }
    }
    
    @Override
    public QueryResponse searchWithFacets(String query, Map<String, String> filters, 
                                          List<String> facetFields, String sortField, String sortOrder,
                                          int start, int rows) {
        try {
            SolrQuery solrQuery = buildQuery(query, filters, sortField, sortOrder, start, rows);
            
            // Add faceting
            if (facetFields != null && !facetFields.isEmpty()) {
                solrQuery.setFacet(true);
                for (String field : facetFields) {
                    solrQuery.addFacetField(field);
                }
                solrQuery.setFacetMinCount(1);
            }
            
            return getSolrClient().query(solrConfig.getCore(), solrQuery);
        } catch (SolrServerException | IOException | RuntimeException e) {
            log.error("Solr faceted search failed: {}", e.getMessage());
            throw new RuntimeException("Solr faceted search failed", e);
        }
    }
    
    @Override
    public SuggesterResponse getSuggestions(String query, int limit) {
        try {
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setRequestHandler("/suggest");
            solrQuery.setParam("q", query);
            solrQuery.setParam("suggest.count", String.valueOf(limit));
            
            QueryResponse response = getSolrClient().query(solrConfig.getCore(), solrQuery);
            return response.getSuggesterResponse();
        } catch (SolrServerException | IOException | RuntimeException e) {
            log.error("Solr suggestions failed: {}", e.getMessage());
            throw new RuntimeException("Solr suggestions failed", e);
        }
    }
    
    @Override
    public void index(BookSearchDocument document) throws Exception {
        try {
            SolrInputDocument solrDoc = buildSolrDocument(document);
            getSolrClient().add(solrConfig.getCore(), solrDoc);
            getSolrClient().commit(solrConfig.getCore());
        } catch (Exception e) {
            log.error("Failed to index document {}: {}", document.getId(), e.getMessage());
            throw e;
        }
    }
    
    @Override
    public void indexBatch(List<BookSearchDocument> documents) throws Exception {
        try {
            List<SolrInputDocument> solrDocs = documents.stream()
                .map(this::buildSolrDocument)
                .collect(Collectors.toList());
            
            getSolrClient().add(solrConfig.getCore(), solrDocs);
            getSolrClient().commit(solrConfig.getCore());
            log.info("Indexed {} documents in batch", documents.size());
        } catch (Exception e) {
            log.error("Failed to index batch of {} documents: {}", documents.size(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Build SolrInputDocument from BookSearchDocument
     * Extracted to avoid code duplication
     */
    private SolrInputDocument buildSolrDocument(BookSearchDocument document) {
        SolrInputDocument solrDoc = new SolrInputDocument();
        solrDoc.addField("id", document.getId());
        if (document.getTitle() != null) solrDoc.addField("title", document.getTitle());
        if (document.getTitleText() != null) solrDoc.addField("titleText", document.getTitleText());
        if (document.getDescription() != null) solrDoc.addField("description", document.getDescription());
        if (document.getDescriptionText() != null) solrDoc.addField("descriptionText", document.getDescriptionText());
        if (document.getIsbn() != null) solrDoc.addField("isbn", document.getIsbn());
        if (document.getAuthorNames() != null) solrDoc.addField("authorNames", document.getAuthorNames());
        if (document.getPublisherName() != null) solrDoc.addField("publisherName", document.getPublisherName());
        if (document.getGenreNames() != null) solrDoc.addField("genreNames", document.getGenreNames());
        if (document.getLanguage() != null) solrDoc.addField("language", document.getLanguage());
        if (document.getFormat() != null) solrDoc.addField("format", document.getFormat());
        if (document.getPhysicalPrice() != null) solrDoc.addField("physicalPrice", document.getPhysicalPrice());
        if (document.getDigitalPrice() != null) solrDoc.addField("digitalPrice", document.getDigitalPrice());
        if (document.getPublicationDate() != null) solrDoc.addField("publicationDate", document.getPublicationDate());
        if (document.getAvailableInHanoi() != null) solrDoc.addField("availableInHanoi", document.getAvailableInHanoi());
        if (document.getAvailableInHcmc() != null) solrDoc.addField("availableInHcmc", document.getAvailableInHcmc());
        if (document.getAvailableInDanang() != null) solrDoc.addField("availableInDanang", document.getAvailableInDanang());
        if (document.getAverageRating() != null) solrDoc.addField("averageRating", document.getAverageRating());
        if (document.getReviewCount() != null) solrDoc.addField("reviewCount", document.getReviewCount());
        if (document.getCreatedAt() != null) solrDoc.addField("createdAt", document.getCreatedAt());
        return solrDoc;
    }
    
    @Override
    public void deleteById(String id) throws Exception {
        try {
            getSolrClient().deleteById(solrConfig.getCore(), id);
            getSolrClient().commit(solrConfig.getCore());
        } catch (Exception e) {
            log.error("Failed to delete document {}: {}", id, e.getMessage());
            throw e;
        }
    }
    
    @Override
    public void deleteByIds(List<String> ids) throws Exception {
        try {
            getSolrClient().deleteById(solrConfig.getCore(), ids);
            getSolrClient().commit(solrConfig.getCore());
            log.info("Deleted {} documents", ids.size());
        } catch (Exception e) {
            log.error("Failed to delete batch of {} documents: {}", ids.size(), e.getMessage());
            throw e;
        }
    }
    
    @Override
    public SolrQuery buildQuery(String queryString, Map<String, String> filters, 
                                String sortField, String sortOrder, int start, int rows) {
        SolrQuery solrQuery = new SolrQuery();
        
        // Set query string (default to *:* if empty)
        if (queryString == null || queryString.trim().isEmpty()) {
            solrQuery.setQuery("*:*");
        } else {
            // Escape query string for Solr (but preserve ~ for fuzzy search)
            String escapedQuery = escapeSolrQueryForField(queryString);
            
            // Build multi-field query for title and description
            // Include both standard fields (title, description) and Vietnamese fields (titleText, descriptionText)
            // Add fuzzy search (~1) for typo tolerance on Vietnamese fields
            // Format: (field:query OR field:query~1) for fuzzy matching
            String multiFieldQuery = String.format(
                "(title:%s OR description:%s OR titleText:%s OR descriptionText:%s OR titleText:%s~1 OR descriptionText:%s~1)", 
                escapedQuery, escapedQuery, escapedQuery, escapedQuery, escapedQuery, escapedQuery);
            solrQuery.setQuery(multiFieldQuery);
        }
        
        // Add filters
        if (filters != null) {
            for (Map.Entry<String, String> filter : filters.entrySet()) {
                solrQuery.addFilterQuery(filter.getKey() + ":" + filter.getValue());
            }
        }
        
        // Set sorting
        if (sortField != null && !sortField.isEmpty()) {
            SolrQuery.SortClause sortClause = sortOrder != null && "desc".equalsIgnoreCase(sortOrder)
                ? SolrQuery.SortClause.desc(sortField)
                : SolrQuery.SortClause.asc(sortField);
            solrQuery.setSort(sortClause);
        } else {
            // Default sort by relevance (score desc)
            solrQuery.setSort(SolrQuery.SortClause.desc("score"));
        }
        
        // Set pagination
        solrQuery.setStart(start);
        solrQuery.setRows(rows);
        
        // Optimize field selection: only return ID field (we fetch full data from DB)
        // This reduces network transfer and improves performance
        solrQuery.setFields("id");
        
        // Enable highlighting for both standard and Vietnamese fields
        solrQuery.setHighlight(true);
        solrQuery.addHighlightField("title");
        solrQuery.addHighlightField("description");
        solrQuery.addHighlightField("titleText");
        solrQuery.addHighlightField("descriptionText");
        solrQuery.setHighlightSimplePre("<em>");
        solrQuery.setHighlightSimplePost("</em>");
        
        // Note: Facet method optimization is configured in solrconfig.xml
        // Using filter queries (fq) instead of query parameters is already optimized
        
        return solrQuery;
    }
    
    /**
     * Escape Solr query for field queries (used in buildQuery)
     * Escapes all special characters except those needed for fuzzy search
     * Note: ~ is not escaped as it's used for fuzzy search operators
     */
    private String escapeSolrQueryForField(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "*";
        }
        // Escape special Solr characters for field queries
        // Preserve ~ for fuzzy search, but escape it if it's not at the end
        String escaped = query.replace("\\", "\\\\")
            .replace("+", "\\+")
            .replace("-", "\\-")
            .replace("&&", "\\&&")
            .replace("||", "\\||")
            .replace("!", "\\!")
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace("{", "\\{")
            .replace("}", "\\}")
            .replace("[", "\\[")
            .replace("]", "\\]")
            .replace("^", "\\^")
            .replace("\"", "\\\"")
            .replace("*", "\\*")
            .replace("?", "\\?")
            .replace(":", "\\:");
        
        // If query ends with ~, preserve it (for fuzzy search)
        // Otherwise, escape any ~ in the middle
        if (!escaped.endsWith("~")) {
            escaped = escaped.replace("~", "\\~");
        }
        
        return escaped;
    }
}

