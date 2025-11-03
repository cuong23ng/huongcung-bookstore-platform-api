package com.huongcung.core.product.entity;

import com.huongcung.core.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "translators")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TranslatorEntity extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "biography", columnDefinition = "TEXT")
    private String biography;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "birth_date")
    private LocalDate birthDate;
}
