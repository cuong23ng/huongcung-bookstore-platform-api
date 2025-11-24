package com.huongcung.core.media.converter;

import com.huongcung.core.media.model.domain.BookImage;
import com.huongcung.core.media.model.domain.Image;
import com.huongcung.core.media.model.dto.BookImageDTO;
import com.huongcung.core.media.model.dto.ImageDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ImageConverter implements Converter<Image, ImageDTO> {

    @Override
    public ImageDTO convert(Image image) {
        ImageDTO imageDTO = null;
        if (image instanceof BookImage) {
            imageDTO = new BookImageDTO();
            populate((BookImage) image, (BookImageDTO) imageDTO);
        } else {
            imageDTO = new ImageDTO();
            populate(image, imageDTO);
        }
        return imageDTO;
    }

    private void populate(Image image, ImageDTO imageDTO) {

    }

    private void populate(BookImage source, BookImageDTO target) {
        target.setPosition(source.getPosition());
        target.setUrl(source.getUrl());
        target.setAltText(source.getAltText());
        target.setCover(source.isCover());
        target.setBackCover(source.isBackCover());
    }
}
