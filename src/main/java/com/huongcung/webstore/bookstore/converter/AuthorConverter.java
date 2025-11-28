package com.huongcung.webstore.bookstore.converter;

import com.huongcung.core.contributor.model.dto.AuthorDTO;
import com.huongcung.webstore.bookstore.model.AuthorFrontPageDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class AuthorConverter implements Converter<AuthorDTO, AuthorFrontPageDTO> {

    @Override
    public AuthorFrontPageDTO convert(AuthorDTO authorDTO) {
        AuthorFrontPageDTO authorFrontPageDTO = new AuthorFrontPageDTO();
        populate(authorDTO, authorFrontPageDTO);
        return authorFrontPageDTO;
    }

    private void populate(AuthorDTO source, AuthorFrontPageDTO target) {
        target.setName(source.getName());
    }
}
