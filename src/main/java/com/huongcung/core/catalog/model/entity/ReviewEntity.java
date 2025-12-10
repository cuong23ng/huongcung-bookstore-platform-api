package com.huongcung.core.catalog.model.entity;

import com.huongcung.core.catalog.enumeration.ReviewStatus;
import com.huongcung.core.common.model.entity.BaseEntity;
import com.huongcung.core.user.model.entity.StaffEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewEntity extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private AbstractBookEntity book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private StaffEntity user;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "title", nullable = true)
    private String title;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "is_ai_generated")
    private Boolean isAiGenerated = false;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "review_sources", joinColumns = @JoinColumn(name = "review_id"))
    private List<ReviewSource> sources;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReviewStatus status = ReviewStatus.DRAFT;
}