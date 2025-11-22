package com.huongcung.core.contributor.model.dto;

import com.huongcung.core.media.model.dto.ImageDTO;
import lombok.Data;

import java.util.Date;

@Data
public class TranslatorDTO {
    private String name;
    private String biography;
    private ImageDTO image;
    private Date birthDate;
}
