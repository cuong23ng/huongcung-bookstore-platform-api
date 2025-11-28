package com.huongcung.core.catalog.converter;

import com.huongcung.core.catalog.model.domain.AbstractBook;
import com.huongcung.core.catalog.model.dto.AbstractBookDTO;
import com.huongcung.core.contributor.converter.GenreConverter;
import com.huongcung.core.contributor.model.dto.AuthorDTO;
import com.huongcung.core.contributor.model.dto.PublisherDTO;
import com.huongcung.core.contributor.model.dto.TranslatorDTO;
import com.huongcung.core.media.converter.ImageConverter;
import com.huongcung.core.media.model.dto.BookImageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookDetailsConverter implements Converter<AbstractBook, AbstractBookDTO> {

    private final PhysicalBookInformationConverter physicalBookInformationConverter;
    private final EbookInformationConverter ebookInformationConverter;
    private final GenreConverter genreConverter;
    private final ImageConverter imageConverter;

    @Override
    public AbstractBookDTO convert(AbstractBook book) {
        AbstractBookDTO bookDTO = new AbstractBookDTO();
        populate(book, bookDTO);
        return bookDTO;
    }

    private void populate(AbstractBook source, AbstractBookDTO target) {
        target.setCode(source.getCode());
        target.setTitle(source.getTitle());
        target.setEdition(source.getEdition());
        target.setLanguage(source.getLanguage());
        target.setPageCount(source.getPageCount());
        target.setDescription(source.getDescription());

        target.setGenres(source.getGenres().parallelStream().map(genreConverter::convert).toList());

        target.setAuthors(source.getAuthors().parallelStream().map(a -> {
            AuthorDTO authorDTO = new AuthorDTO();
            authorDTO.setName(a.getName());
            return authorDTO;
        }).toList());

        target.setTranslators(source.getTranslators().parallelStream().map(a -> {
            TranslatorDTO translatorDTO = new TranslatorDTO();
            translatorDTO.setName(a.getName());
            return translatorDTO;
        }).toList());

        PublisherDTO publisherDTO = new PublisherDTO();
        publisherDTO.setName(source.getPublisher().getName());
        target.setPublisher(publisherDTO);

        List<BookImageDTO> imageDTOS = source.getImages()
                .stream().map(imageConverter::convert)
                .map(imageDTO -> (BookImageDTO) imageDTO)
                .toList();
        target.setImages(imageDTOS);

        if (source.hasPhysicalEdition()) {
            target.setPhysicalBookInfo(physicalBookInformationConverter.convert(source.getPhysicalBookInfo()));
        }

        if (source.hasEbookEdition()) {
            target.setEbookInfo(ebookInformationConverter.convert(source.getEbookInfo()));
        }
    }
}
