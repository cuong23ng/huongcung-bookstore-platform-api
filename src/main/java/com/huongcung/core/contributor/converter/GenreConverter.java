package com.huongcung.core.contributor.converter;

import com.huongcung.core.contributor.model.domain.Genre;
import com.huongcung.core.contributor.model.dto.GenreDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class GenreConverter implements Converter<Genre, GenreDTO> {
    @Override
    public GenreDTO convert(Genre source) {
        return null;
    }
}
