package com.huongcung.core.contributor.model.dto;

import com.huongcung.core.media.model.dto.ImageDTO;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AuthorDTO {
    private String name;
    private String biography;
    private ImageDTO image;
    private LocalDate birthDate;
    private String nationality;
}
