package com.huongcung.core.contributor.model.domain;

import com.huongcung.core.media.model.domain.Image;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class Author {
    private Long id;
    private String name;
    private String biography;
    private Image image;
    private LocalDate birthDate;
    private String nationality;
}
