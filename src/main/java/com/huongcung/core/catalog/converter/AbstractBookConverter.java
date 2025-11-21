package com.huongcung.core.catalog.converter;

import com.huongcung.core.catalog.model.domain.AbstractBook;
import com.huongcung.core.catalog.model.domain.EbookInformation;
import com.huongcung.core.catalog.model.domain.PhysicalBookInformation;
import com.huongcung.core.catalog.model.dto.BookFrontPageDTO;
import com.huongcung.core.contributor.model.dto.AuthorDTO;
import com.huongcung.core.media.model.domain.BookImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AbstractBookConverter implements Converter<AbstractBook, BookFrontPageDTO> {

    @Override
    public BookFrontPageDTO convert(AbstractBook abstractBook) {
        BookFrontPageDTO abstractBookDTO = new BookFrontPageDTO();
        populate(abstractBook, abstractBookDTO);
        return abstractBookDTO;
    }

    private void populate(AbstractBook source, BookFrontPageDTO target) {
        target.setCode(source.getCode());
        target.setTitle(source.getTitle());
        target.setAuthors(source.getAuthors().parallelStream().map(a -> {
            AuthorDTO authorDTO = new AuthorDTO();
            authorDTO.setName(a.getName());
            authorDTO.setId(a.getId());
            return authorDTO;
        }).toList());
        target.setCoverUrl(source.getImages()
                .stream()
                .filter(BookImage::isCover)
                .map(BookImage::getUrl)
                .findFirst().orElse(null)
        );
        
        // Handle physical book price - may be null if book is ebook-only
        PhysicalBookInformation physicalInfo = source.getPhysicalBookInfo();
        log.info("PhysicalBookInformation {}", physicalInfo == null);
        if (physicalInfo != null) {
            target.setPhysicalPrice(physicalInfo.getCurrentPrice());
        } else {
            target.setPhysicalPrice(null);
        }
        
        // Handle ebook price - may be null if book is physical-only
        EbookInformation ebookInfo = source.getEbookInfo();
        if (ebookInfo != null) {
            target.setEbookPrice(ebookInfo.getCurrentPrice());
        } else {
            target.setEbookPrice(null);
        }
    }
}
