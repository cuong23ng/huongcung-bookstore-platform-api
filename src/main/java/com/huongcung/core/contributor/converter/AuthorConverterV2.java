package com.huongcung.core.contributor.converter;

import com.huongcung.core.contributor.model.domain.Author;
import com.huongcung.core.contributor.model.dto.AuthorDTO;
import com.huongcung.core.media.converter.ImageConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthorConverterV2 implements Converter<Author, AuthorDTO> {

    private final ImageConverter imageConverter;

    @Override
    public AuthorDTO convert(Author author) {
        AuthorDTO authorDTO = new AuthorDTO();
        populate(author, authorDTO);
        return authorDTO;
    }

    private void populate(Author source, AuthorDTO target) {
        target.setName(source.getName());
        target.setBiography(source.getBiography());
        target.setImage(imageConverter.convert(source.getImage()));
        target.setBirthDate(source.getBirthDate());
        target.setNationality(source.getNationality());
    }
}
