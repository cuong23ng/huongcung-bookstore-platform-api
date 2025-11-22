package com.huongcung.core.media.converter;

import com.huongcung.core.media.model.domain.Image;
import com.huongcung.core.media.model.dto.ImageDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ImageConverter implements Converter<Image, ImageDTO> {
    @Override
    public ImageDTO convert(Image image) {
        return null;
    }
}
