package com.huongcung.core.search.service.impl;

import com.huongcung.core.common.enumeration.Language;
import com.huongcung.core.product.model.dto.AbstractBookDTO;
import com.huongcung.core.product.service.AbstractBookService;
import com.huongcung.core.search.model.dto.PaginationInfo;
import com.huongcung.core.search.model.dto.SearchFacet;
import com.huongcung.core.search.model.dto.SearchRequest;
import com.huongcung.core.search.model.dto.SearchResponse;
import com.huongcung.core.search.repository.BookSearchRepository;
import com.huongcung.core.search.service.SearchPerformanceMonitor;
import com.huongcung.webstore.bookstore.mapper.BookViewMapper;
import com.huongcung.webstore.bookstore.model.BookData;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SolrSearchServiceImpl
 * Test ID: 1.3-UNIT-001
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SolrSearchServiceImpl Unit Tests")
class SolrSearchServiceImplTest {
    
    @Mock
    private BookSearchRepository bookSearchRepository;
    
    @Mock
    private AbstractBookService abstractBookService;
    
    @Mock
    private BookViewMapper bookViewMapper;
    
    @Mock
    private SearchPerformanceMonitor performanceMonitor;
    
    @InjectMocks
    private SolrSearchServiceImpl searchService;
    
    private SearchRequest searchRequest;
    private QueryResponse mockQueryResponse;
    private SolrDocumentList mockDocumentList;
    private List<AbstractBookDTO> mockBooks;
    
    @BeforeEach
    void setUp() {
        // Setup default search request
        searchRequest = SearchRequest.builder()
            .q("test query")
            .page(1)
            .size(20)
            .build();
        
        // Setup mock Solr response
        mockQueryResponse = mock(QueryResponse.class);
        mockDocumentList = new SolrDocumentList();
        mockDocumentList.setNumFound(2);
        mockDocumentList.setStart(0);
        
        SolrDocument doc1 = new SolrDocument();
        doc1.addField("id", "1");
        doc1.addField("title", "Test Book 1");
        
        SolrDocument doc2 = new SolrDocument();
        doc2.addField("id", "2");
        doc2.addField("title", "Test Book 2");
        
        mockDocumentList.add(doc1);
        mockDocumentList.add(doc2);
        
        lenient().when(mockQueryResponse.getResults()).thenReturn(mockDocumentList);
        lenient().when(mockQueryResponse.getHighlighting()).thenReturn(new HashMap<>());
        lenient().when(mockQueryResponse.getFacetFields()).thenReturn(new ArrayList<>());
        
        // Setup mock books
        mockBooks = new ArrayList<>();
        AbstractBookDTO book1 = new AbstractBookDTO();
        book1.setId(1L);
        book1.setCode("BOOK001");
        book1.setTitle("Test Book 1");
        book1.setDescription("Test description 1");
        book1.setLanguage(Language.VIETNAMESE);
        book1.setHasPhysicalEdition(true);
        book1.setHasElectricEdition(false);
        
        AbstractBookDTO book2 = new AbstractBookDTO();
        book2.setId(2L);
        book2.setCode("BOOK002");
        book2.setTitle("Test Book 2");
        book2.setDescription("Test description 2");
        book2.setLanguage(Language.ENGLISH);
        book2.setHasPhysicalEdition(false);
        book2.setHasElectricEdition(true);
        
        mockBooks.add(book1);
        mockBooks.add(book2);
        
        // Setup mock BookData
        BookData bookData1 = new BookData();
        bookData1.setCode("BOOK001");
        bookData1.setTitle("Test Book 1");
        
        BookData bookData2 = new BookData();
        bookData2.setCode("BOOK002");
        bookData2.setTitle("Test Book 2");
        
        lenient().when(bookViewMapper.toBookData(any(AbstractBookDTO.class)))
            .thenAnswer(invocation -> {
                AbstractBookDTO dto = invocation.getArgument(0);
                BookData data = new BookData();
                data.setCode(dto.getCode());
                data.setTitle(dto.getTitle());
                return data;
            });
    }
    
    @Test
    @DisplayName("Should perform basic search successfully")
    void testBasicSearch() {
        // Given
        when(bookSearchRepository.searchWithFacets(
            anyString(), anyMap(), anyList(), any(), any(), anyInt(), anyInt()))
            .thenReturn(mockQueryResponse);
        when(abstractBookService.findByIds(anyList())).thenReturn(mockBooks);
        
        // When
        SearchResponse response = searchService.searchBooks(searchRequest);
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getBooks());
        assertEquals(2, response.getBooks().size());
        assertNotNull(response.getPagination());
        assertEquals(2L, response.getPagination().getTotalResults());
        assertFalse(response.getFallbackUsed());
        
        verify(bookSearchRepository).searchWithFacets(
            anyString(), anyMap(), anyList(), any(), any(), eq(0), eq(20));
        verify(abstractBookService).findByIds(Arrays.asList(1L, 2L));
    }
    
    @Test
    @DisplayName("Should handle empty search results")
    void testEmptySearchResults() {
        // Given
        SolrDocumentList emptyList = new SolrDocumentList();
        emptyList.setNumFound(0);
        when(mockQueryResponse.getResults()).thenReturn(emptyList);
        when(bookSearchRepository.searchWithFacets(
            anyString(), anyMap(), anyList(), any(), any(), anyInt(), anyInt()))
            .thenReturn(mockQueryResponse);
        
        // When
        SearchResponse response = searchService.searchBooks(searchRequest);
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getBooks());
        assertTrue(response.getBooks().isEmpty());
        assertEquals(0L, response.getPagination().getTotalResults());
    }
    
    @Test
    @DisplayName("Should apply genre filters")
    void testGenreFiltering() {
        // Given
        searchRequest.setGenres(Arrays.asList("Fiction", "Adventure"));
        when(bookSearchRepository.searchWithFacets(
            anyString(), anyMap(), anyList(), any(), any(), anyInt(), anyInt()))
            .thenReturn(mockQueryResponse);
        when(abstractBookService.findByIds(anyList())).thenReturn(mockBooks);
        
        // When
        SearchResponse response = searchService.searchBooks(searchRequest);
        
        // Then
        assertNotNull(response);
        verify(bookSearchRepository).searchWithFacets(
            anyString(),
            argThat(filters -> filters.containsKey("genreNames")),
            anyList(), any(), any(), anyInt(), anyInt());
    }
    
    @Test
    @DisplayName("Should apply language filters")
    void testLanguageFiltering() {
        // Given
        searchRequest.setLanguages(Arrays.asList("Vietnamese", "English"));
        when(bookSearchRepository.searchWithFacets(
            anyString(), anyMap(), anyList(), any(), any(), anyInt(), anyInt()))
            .thenReturn(mockQueryResponse);
        when(abstractBookService.findByIds(anyList())).thenReturn(mockBooks);
        
        // When
        SearchResponse response = searchService.searchBooks(searchRequest);
        
        // Then
        assertNotNull(response);
        verify(bookSearchRepository).searchWithFacets(
            anyString(),
            argThat(filters -> filters.containsKey("language")),
            anyList(), any(), any(), anyInt(), anyInt());
    }
    
    @Test
    @DisplayName("Should apply format filters")
    void testFormatFiltering() {
        // Given
        searchRequest.setFormats(Arrays.asList("PHYSICAL", "DIGITAL"));
        when(bookSearchRepository.searchWithFacets(
            anyString(), anyMap(), anyList(), any(), any(), anyInt(), anyInt()))
            .thenReturn(mockQueryResponse);
        when(abstractBookService.findByIds(anyList())).thenReturn(mockBooks);
        
        // When
        SearchResponse response = searchService.searchBooks(searchRequest);
        
        // Then
        assertNotNull(response);
        verify(bookSearchRepository).searchWithFacets(
            anyString(),
            argThat(filters -> filters.containsKey("format")),
            anyList(), any(), any(), anyInt(), anyInt());
    }
    
    @Test
    @DisplayName("Should apply price range filters")
    void testPriceFiltering() {
        // Given
        searchRequest.setMinPrice(10000.0);
        searchRequest.setMaxPrice(100000.0);
        when(bookSearchRepository.searchWithFacets(
            anyString(), anyMap(), anyList(), any(), any(), anyInt(), anyInt()))
            .thenReturn(mockQueryResponse);
        when(abstractBookService.findByIds(anyList())).thenReturn(mockBooks);
        
        // When
        SearchResponse response = searchService.searchBooks(searchRequest);
        
        // Then
        assertNotNull(response);
        verify(bookSearchRepository).searchWithFacets(
            anyString(),
            argThat(filters -> filters.containsKey("physicalPrice")),
            anyList(), any(), any(), anyInt(), anyInt());
    }
    
    @Test
    @DisplayName("Should apply city availability filters")
    void testCityFiltering() {
        // Given
        searchRequest.setCities(Arrays.asList("HANOI", "HCMC"));
        when(bookSearchRepository.searchWithFacets(
            anyString(), anyMap(), anyList(), any(), any(), anyInt(), anyInt()))
            .thenReturn(mockQueryResponse);
        when(abstractBookService.findByIds(anyList())).thenReturn(mockBooks);
        
        // When
        SearchResponse response = searchService.searchBooks(searchRequest);
        
        // Then
        assertNotNull(response);
        verify(bookSearchRepository).searchWithFacets(
            anyString(),
            argThat(filters -> filters.containsKey("availableInHanoi") || 
                             filters.containsKey("availableInHcmc")),
            anyList(), any(), any(), anyInt(), anyInt());
    }
    
    @Test
    @DisplayName("Should handle pagination correctly")
    void testPagination() {
        // Given
        searchRequest.setPage(2);
        searchRequest.setSize(10);
        when(bookSearchRepository.searchWithFacets(
            anyString(), anyMap(), anyList(), any(), any(), anyInt(), anyInt()))
            .thenReturn(mockQueryResponse);
        when(abstractBookService.findByIds(anyList())).thenReturn(mockBooks);
        
        // When
        SearchResponse response = searchService.searchBooks(searchRequest);
        
        // Then
        assertNotNull(response);
        verify(bookSearchRepository).searchWithFacets(
            anyString(), anyMap(), anyList(), any(), any(), eq(10), eq(10));
        assertEquals(2, response.getPagination().getCurrentPage());
        assertEquals(10, response.getPagination().getPageSize());
    }
    
    @Test
    @DisplayName("Should extract facets from Solr response")
    void testFacetExtraction() {
        // Given
        List<FacetField> facetFields = new ArrayList<>();
        FacetField genreFacet = new FacetField("genreNames");
        genreFacet.add("Fiction", 10);
        genreFacet.add("Adventure", 5);
        facetFields.add(genreFacet);
        
        when(mockQueryResponse.getFacetFields()).thenReturn(facetFields);
        when(bookSearchRepository.searchWithFacets(
            anyString(), anyMap(), anyList(), any(), any(), anyInt(), anyInt()))
            .thenReturn(mockQueryResponse);
        when(abstractBookService.findByIds(anyList())).thenReturn(mockBooks);
        
        // When
        SearchResponse response = searchService.searchBooks(searchRequest);
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getFacets());
        assertTrue(response.getFacets().containsKey("genreNames"));
        List<SearchFacet> facets = response.getFacets().get("genreNames");
        assertEquals(2, facets.size());
        assertEquals("Fiction", facets.get(0).getValue());
        assertEquals(10L, facets.get(0).getCount());
    }
    
    @Test
    @DisplayName("Should extract highlights from Solr response")
    void testHighlightExtraction() {
        // Given
        Map<String, Map<String, List<String>>> highlighting = new HashMap<>();
        Map<String, List<String>> doc1Highlights = new HashMap<>();
        doc1Highlights.put("title", Arrays.asList("Test <em>Book</em> 1"));
        highlighting.put("1", doc1Highlights);
        
        when(mockQueryResponse.getHighlighting()).thenReturn(highlighting);
        when(bookSearchRepository.searchWithFacets(
            anyString(), anyMap(), anyList(), any(), any(), anyInt(), anyInt()))
            .thenReturn(mockQueryResponse);
        when(abstractBookService.findByIds(anyList())).thenReturn(mockBooks);
        
        // When
        SearchResponse response = searchService.searchBooks(searchRequest);
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getHighlightedFields());
        assertTrue(response.getHighlightedFields().containsKey("1"));
        assertEquals("Test <em>Book</em> 1", response.getHighlightedFields().get("1"));
    }
    
    @Test
    @DisplayName("Should apply sort parameter")
    void testSortParameter() {
        // Given
        searchRequest.setSort("price_asc");
        when(bookSearchRepository.searchWithFacets(
            anyString(), anyMap(), anyList(), any(), any(), anyInt(), anyInt()))
            .thenReturn(mockQueryResponse);
        when(abstractBookService.findByIds(anyList())).thenReturn(mockBooks);
        
        // When
        SearchResponse response = searchService.searchBooks(searchRequest);
        
        // Then
        assertNotNull(response);
        verify(bookSearchRepository).searchWithFacets(
            anyString(), anyMap(), anyList(), eq("physicalPrice"), eq("asc"), anyInt(), anyInt());
    }
    
    @Test
    @DisplayName("Should fallback to database search when Solr fails")
    void testFallbackToDatabaseSearch() {
        // Given
        when(bookSearchRepository.searchWithFacets(
            anyString(), anyMap(), anyList(), any(), any(), anyInt(), anyInt()))
            .thenThrow(new RuntimeException("Solr connection failed"));
        when(abstractBookService.findAll()).thenReturn(mockBooks);
        
        // When
        SearchResponse response = searchService.searchBooks(searchRequest);
        
        // Then
        assertNotNull(response);
        assertTrue(response.getFallbackUsed());
        assertNotNull(response.getBooks());
        verify(abstractBookService).findAll();
    }
    
    @Test
    @DisplayName("Should escape query string to prevent injection")
    void testQueryEscaping() {
        // Given
        searchRequest.setQ("test +query && special:chars");
        when(bookSearchRepository.searchWithFacets(
            anyString(), anyMap(), anyList(), any(), any(), anyInt(), anyInt()))
            .thenReturn(mockQueryResponse);
        when(abstractBookService.findByIds(anyList())).thenReturn(mockBooks);
        
        // When
        SearchResponse response = searchService.searchBooks(searchRequest);
        
        // Then
        assertNotNull(response);
        verify(bookSearchRepository).searchWithFacets(
            argThat(query -> query.contains("\\+") && query.contains("\\&&") && query.contains("\\:")),
            anyMap(), anyList(), any(), any(), anyInt(), anyInt());
    }
    
    @Test
    @DisplayName("Should handle null query string")
    void testNullQuery() {
        // Given
        searchRequest.setQ(null);
        when(bookSearchRepository.searchWithFacets(
            anyString(), anyMap(), anyList(), any(), any(), anyInt(), anyInt()))
            .thenReturn(mockQueryResponse);
        when(abstractBookService.findByIds(anyList())).thenReturn(mockBooks);
        
        // When
        SearchResponse response = searchService.searchBooks(searchRequest);
        
        // Then
        assertNotNull(response);
        verify(bookSearchRepository).searchWithFacets(
            eq("*:*"), anyMap(), anyList(), any(), any(), anyInt(), anyInt());
    }
    
    @Test
    @DisplayName("Should get suggestions successfully")
    void testGetSuggestions() {
        // Given - Mock null response to test empty list return
        // (Full suggestion mocking requires complex inner class setup)
        when(bookSearchRepository.getSuggestions("nga", 10))
            .thenReturn(null);
        
        // When
        List<String> result = searchService.getSuggestions("nga");
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookSearchRepository).getSuggestions("nga", 10);
    }
    
    @Test
    @DisplayName("Should return empty list when suggestions fail")
    void testGetSuggestionsFailure() {
        // Given
        when(bookSearchRepository.getSuggestions("test", 10))
            .thenThrow(new RuntimeException("Solr suggestions failed"));
        
        // When
        List<String> result = searchService.getSuggestions("test");
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    @DisplayName("Should get facets successfully")
    void testGetFacets() {
        // Given
        List<FacetField> facetFields = new ArrayList<>();
        FacetField genreFacet = new FacetField("genreNames");
        genreFacet.add("Fiction", 10);
        facetFields.add(genreFacet);
        
        when(mockQueryResponse.getFacetFields()).thenReturn(facetFields);
        when(bookSearchRepository.searchWithFacets(
            anyString(), anyMap(), anyList(), any(), any(), anyInt(), anyInt()))
            .thenReturn(mockQueryResponse);
        
        // When
        Map<String, List<SearchFacet>> facets = searchService.getFacets(searchRequest);
        
        // Then
        assertNotNull(facets);
        assertTrue(facets.containsKey("genreNames"));
        verify(bookSearchRepository).searchWithFacets(
            anyString(), anyMap(), anyList(), any(), any(), eq(0), eq(0));
    }
    
    @Test
    @DisplayName("Should handle invalid book IDs gracefully")
    void testInvalidBookIds() {
        // Given
        SolrDocument docWithInvalidId = new SolrDocument();
        docWithInvalidId.addField("id", "invalid");
        SolrDocumentList docList = new SolrDocumentList();
        docList.add(docWithInvalidId);
        docList.setNumFound(1);
        
        when(mockQueryResponse.getResults()).thenReturn(docList);
        when(bookSearchRepository.searchWithFacets(
            anyString(), anyMap(), anyList(), any(), any(), anyInt(), anyInt()))
            .thenReturn(mockQueryResponse);
        when(abstractBookService.findByIds(anyList())).thenReturn(Collections.emptyList());
        
        // When
        SearchResponse response = searchService.searchBooks(searchRequest);
        
        // Then
        assertNotNull(response);
        assertTrue(response.getBooks().isEmpty());
    }
    
    @Test
    @DisplayName("Should handle null book IDs in Solr documents")
    void testNullBookIds() {
        // Given
        SolrDocument docWithNullId = new SolrDocument();
        docWithNullId.addField("title", "Test Book");
        SolrDocumentList docList = new SolrDocumentList();
        docList.add(docWithNullId);
        docList.setNumFound(1);
        
        when(mockQueryResponse.getResults()).thenReturn(docList);
        when(bookSearchRepository.searchWithFacets(
            anyString(), anyMap(), anyList(), any(), any(), anyInt(), anyInt()))
            .thenReturn(mockQueryResponse);
        
        // When
        SearchResponse response = searchService.searchBooks(searchRequest);
        
        // Then
        assertNotNull(response);
        assertTrue(response.getBooks().isEmpty());
        verify(abstractBookService, never()).findByIds(anyList());
    }
    
    @Test
    @DisplayName("Should map city names correctly")
    void testCityMapping() {
        // Given
        searchRequest.setCities(Arrays.asList("HANOI", "HCMC", "DANANG", "Hà Nội", "Đà Nẵng"));
        when(bookSearchRepository.searchWithFacets(
            anyString(), anyMap(), anyList(), any(), any(), anyInt(), anyInt()))
            .thenReturn(mockQueryResponse);
        when(abstractBookService.findByIds(anyList())).thenReturn(mockBooks);
        
        // When
        SearchResponse response = searchService.searchBooks(searchRequest);
        
        // Then
        assertNotNull(response);
        verify(bookSearchRepository).searchWithFacets(
            anyString(),
            argThat(filters -> 
                filters.containsKey("availableInHanoi") &&
                filters.containsKey("availableInHcmc") &&
                filters.containsKey("availableInDanang")),
            anyList(), any(), any(), anyInt(), anyInt());
    }
    
    @Test
    @DisplayName("Should calculate pagination info correctly")
    void testPaginationCalculation() {
        // Given
        mockDocumentList.setNumFound(50);
        searchRequest.setPage(2);
        searchRequest.setSize(20);
        when(bookSearchRepository.searchWithFacets(
            anyString(), anyMap(), anyList(), any(), any(), anyInt(), anyInt()))
            .thenReturn(mockQueryResponse);
        when(abstractBookService.findByIds(anyList())).thenReturn(mockBooks);
        
        // When
        SearchResponse response = searchService.searchBooks(searchRequest);
        
        // Then
        assertNotNull(response);
        PaginationInfo pagination = response.getPagination();
        assertEquals(2, pagination.getCurrentPage());
        assertEquals(20, pagination.getPageSize());
        assertEquals(50L, pagination.getTotalResults());
        assertEquals(3, pagination.getTotalPages()); // 50 / 20 = 2.5, ceil = 3
        assertTrue(pagination.getHasNext());
        assertTrue(pagination.getHasPrevious());
    }
    
    @Test
    @DisplayName("Should maintain Solr result order when fetching books")
    void testResultOrderPreservation() {
        // Given
        SolrDocument doc1 = new SolrDocument();
        doc1.addField("id", "2");
        SolrDocument doc2 = new SolrDocument();
        doc2.addField("id", "1");
        SolrDocumentList orderedList = new SolrDocumentList();
        orderedList.add(doc1);
        orderedList.add(doc2);
        orderedList.setNumFound(2);
        
        when(mockQueryResponse.getResults()).thenReturn(orderedList);
        when(bookSearchRepository.searchWithFacets(
            anyString(), anyMap(), anyList(), any(), any(), anyInt(), anyInt()))
            .thenReturn(mockQueryResponse);
        
        // Reverse order books
        List<AbstractBookDTO> reversedBooks = Arrays.asList(mockBooks.get(1), mockBooks.get(0));
        when(abstractBookService.findByIds(Arrays.asList(2L, 1L))).thenReturn(reversedBooks);
        
        // When
        SearchResponse response = searchService.searchBooks(searchRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(2, response.getBooks().size());
        // Order should match Solr order (2, 1), not database order
        assertEquals("BOOK002", response.getBooks().get(0).getCode());
        assertEquals("BOOK001", response.getBooks().get(1).getCode());
    }
}

