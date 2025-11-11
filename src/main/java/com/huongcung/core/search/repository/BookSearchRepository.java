package com.huongcung.core.search.repository;

import com.huongcung.core.search.model.entity.BookSearchDocument;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SuggesterResponse;

import java.util.List;
import java.util.Map;

/**
 * Repository interface for Solr book search operations
 * Uses SolrJ client for interaction with Solr server
 */
public interface BookSearchRepository {
    
    /**
     * Perform a basic search query
     * 
     * @param query Solr query string
     * @param start Starting offset for pagination
     * @param rows Number of results to return
     * @return QueryResponse containing search results
     */
    QueryResponse search(String query, int start, int rows);
    
    /**
     * Perform a faceted search with filters
     * 
     * @param query Solr query string
     * @param filters Map of field names to filter values
     * @param facetFields List of fields to facet on
     * @param sortField Field to sort by (null for relevance)
     * @param sortOrder Sort order: "asc" or "desc"
     * @param start Starting offset for pagination
     * @param rows Number of results to return
     * @return QueryResponse containing search results and facets
     */
    QueryResponse searchWithFacets(String query, Map<String, String> filters, 
                                    List<String> facetFields, String sortField, String sortOrder,
                                    int start, int rows);
    
    /**
     * Get autocomplete/suggestion results
     * 
     * @param query Partial query string for suggestions
     * @param limit Maximum number of suggestions to return
     * @return SuggesterResponse containing suggestions
     */
    SuggesterResponse getSuggestions(String query, int limit);
    
    /**
     * Index a book document
     * 
     * @param document BookSearchDocument to index
     * @throws Exception if indexing fails
     */
    void index(BookSearchDocument document) throws Exception;
    
    /**
     * Index multiple book documents
     * 
     * @param documents List of BookSearchDocument to index
     * @throws Exception if indexing fails
     */
    void indexBatch(List<BookSearchDocument> documents) throws Exception;
    
    /**
     * Delete a book document by ID
     * 
     * @param id Document ID to delete
     * @throws Exception if deletion fails
     */
    void deleteById(String id) throws Exception;
    
    /**
     * Delete multiple book documents by IDs
     * 
     * @param ids List of document IDs to delete
     * @throws Exception if deletion fails
     */
    void deleteByIds(List<String> ids) throws Exception;
    
    /**
     * Build a SolrQuery with common search parameters
     * 
     * @param queryString Query string
     * @param filters Map of field names to filter values
     * @param sortField Field to sort by
     * @param sortOrder Sort order (asc/desc)
     * @param start Starting offset
     * @param rows Number of results
     * @return Configured SolrQuery
     */
    SolrQuery buildQuery(String queryString, Map<String, String> filters, 
                         String sortField, String sortOrder, int start, int rows);
}

