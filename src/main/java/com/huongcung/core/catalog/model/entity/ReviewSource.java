package com.huongcung.core.catalog.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSource {
    @Column(name = "title")
    private String title;
    
    @Column(name = "url")
    private String url;
}
