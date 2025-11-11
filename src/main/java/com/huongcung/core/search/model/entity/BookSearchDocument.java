package com.huongcung.core.search.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.solr.client.solrj.beans.Field;

import java.util.Date;
import java.util.List;

/**
 * Solr document model for book search
 * Maps to Solr core "books" schema defined in Story 1.1
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchDocument {
    
    /**
     * Book ID (unique identifier)
     * Maps to Solr field: id (string, required, unique)
     */
    @Field("id")
    private String id;
    
    /**
     * Book title (searchable, highlightable)
     * Maps to Solr field: title (text_general)
     */
    @Field("title")
    private String title;
    
    /**
     * Book title for Vietnamese text analysis
     * Maps to Solr field: titleText (text_vi)
     */
    @Field("titleText")
    private String titleText;
    
    /**
     * Book description (searchable, highlightable)
     * Maps to Solr field: description (text_general)
     */
    @Field("description")
    private String description;
    
    /**
     * Book description for Vietnamese text analysis
     * Maps to Solr field: descriptionText (text_vi)
     */
    @Field("descriptionText")
    private String descriptionText;
    
    /**
     * ISBN number
     * Maps to Solr field: isbn (string)
     */
    @Field("isbn")
    private String isbn;
    
    /**
     * Author names (multi-valued for co-authors)
     * Maps to Solr field: authorNames (string, multi-valued)
     */
    @Field("authorNames")
    private List<String> authorNames;
    
    /**
     * Publisher name
     * Maps to Solr field: publisherName (string)
     */
    @Field("publisherName")
    private String publisherName;
    
    /**
     * Genre/category names (multi-valued)
     * Maps to Solr field: genreNames (string, multi-valued, facetable)
     */
    @Field("genreNames")
    private List<String> genreNames;
    
    /**
     * Language (e.g., "Vietnamese", "English")
     * Maps to Solr field: language (string, facetable)
     */
    @Field("language")
    private String language;
    
    /**
     * Format: PHYSICAL, DIGITAL, or BOTH
     * Maps to Solr field: format (string, facetable)
     */
    @Field("format")
    private String format;
    
    /**
     * Physical book price
     * Maps to Solr field: physicalPrice (pdouble)
     */
    @Field("physicalPrice")
    private Double physicalPrice;
    
    /**
     * Digital/e-book price
     * Maps to Solr field: digitalPrice (pdouble)
     */
    @Field("digitalPrice")
    private Double digitalPrice;
    
    /**
     * Publication date
     * Maps to Solr field: publicationDate (pdate)
     */
    @Field("publicationDate")
    private Date publicationDate;
    
    /**
     * Available in Hanoi
     * Maps to Solr field: availableInHanoi (boolean)
     */
    @Field("availableInHanoi")
    private Boolean availableInHanoi;
    
    /**
     * Available in Ho Chi Minh City
     * Maps to Solr field: availableInHcmc (boolean)
     */
    @Field("availableInHcmc")
    private Boolean availableInHcmc;
    
    /**
     * Available in Da Nang
     * Maps to Solr field: availableInDanang (boolean)
     */
    @Field("availableInDanang")
    private Boolean availableInDanang;
    
    /**
     * Average rating (optional, for future use)
     * Maps to Solr field: averageRating (pdouble)
     */
    @Field("averageRating")
    private Double averageRating;
    
    /**
     * Review count (optional, for future use)
     * Maps to Solr field: reviewCount (pint)
     */
    @Field("reviewCount")
    private Integer reviewCount;
    
    /**
     * Created timestamp
     * Maps to Solr field: createdAt (pdate)
     */
    @Field("createdAt")
    private Date createdAt;
}

