package com.huongcung.core.catalog.converter;

import com.huongcung.core.catalog.model.domain.AbstractBook;
import com.huongcung.core.catalog.model.dto.AbstractBookDTO;
import com.huongcung.core.catalog.model.dto.BookReviewDTO;
import com.huongcung.core.catalog.model.dto.ReviewSourceDTO;
import com.huongcung.core.catalog.model.entity.ReviewSource;
import com.huongcung.core.catalog.enumeration.ReviewStatus;
import com.huongcung.core.contributor.converter.GenreConverter;
import com.huongcung.core.contributor.model.dto.AuthorDTO;
import com.huongcung.core.contributor.model.dto.PublisherDTO;
import com.huongcung.core.contributor.model.dto.TranslatorDTO;
import com.huongcung.core.media.converter.ImageConverter;
import com.huongcung.core.media.model.domain.BookImage;
import com.huongcung.core.media.model.dto.BookImageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
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
        target.setId(source.getId());
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

        // Force load images to avoid lazy initialization issues
        List<BookImageDTO> imageDTOS = new ArrayList<>();
        if (source.getImages() != null) {
            // Force load by accessing the collection
            List<BookImage> images = new ArrayList<>(source.getImages());
            imageDTOS = images.stream()
                    .map(imageConverter::convert)
                    .map(imageDTO -> (BookImageDTO) imageDTO)
                    .toList();
        }
        target.setImages(imageDTOS);

        if (source.hasPhysicalEdition()) {
            target.setHasPhysicalEdition(true);
            target.setPhysicalBookInfo(physicalBookInformationConverter.convert(source.getPhysicalBookInfo()));
        }

        if (source.hasEbookEdition()) {
            target.setHasEbookEdition(true);
            target.setEbookInfo(ebookInformationConverter.convert(source.getEbookInfo()));
        }

        // Include review only if it exists and is PUBLISHED
        if (source.getReview() != null && source.getReview().getStatus() == ReviewStatus.PUBLISHED) {
            // Convert ReviewSource to ReviewSourceDTO and force load to avoid lazy initialization
            List<ReviewSourceDTO> sourceDTOs = new ArrayList<>();
            if (source.getReview().getSources() != null) {
                // Force load by copying to new ArrayList
                List<ReviewSource> sources = new ArrayList<>(source.getReview().getSources());
                for (ReviewSource reviewSource : sources) {
                    ReviewSourceDTO sourceDTO = ReviewSourceDTO.builder()
                            .title(reviewSource.getTitle())
                            .url(reviewSource.getUrl())
                            .build();
                    sourceDTOs.add(sourceDTO);
                }
            }
            
            BookReviewDTO reviewDTO = BookReviewDTO.builder()
                    .title(source.getReview().getTitle())
                    .content(source.getReview().getComment())
                    .sources(sourceDTOs)
                    .build();
            target.setReview(reviewDTO);
        }
    }
}
