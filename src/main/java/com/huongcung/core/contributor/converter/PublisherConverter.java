package com.huongcung.core.contributor.converter;

import com.huongcung.core.contributor.model.domain.Publisher;
import com.huongcung.core.contributor.model.dto.PublisherDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class PublisherConverter implements Converter<Publisher, PublisherDTO> {
    @Override
    public PublisherDTO convert(Publisher source) {
        return null;
    }
}
