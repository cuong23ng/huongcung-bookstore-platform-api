package com.huongcung.core.contributor.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class GenreDTO {
    private String code;
    private String description;
    private GenreDTO parent;
    private List<GenreDTO> children;
}
