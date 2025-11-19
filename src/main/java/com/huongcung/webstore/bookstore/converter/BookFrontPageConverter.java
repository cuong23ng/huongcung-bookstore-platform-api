package com.huongcung.webstore.bookstore.converter;

import com.huongcung.core.storage.service.StorageService;
import com.huongcung.webstore.bookstore.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookFrontPageConverter implements Converter<BookFrontPageDTO, BookData> {

    private final StorageService storageService;

    @Override
    public BookData convert(BookFrontPageDTO source) {
        BookData target = new BookData();
        populate(source, target);
        return target;
    }

    private void populate(BookFrontPageDTO source, BookData target) {
        target.setCode(source.getCode());
        target.setTitle(source.getTitle());
        BookImageData cover = new BookImageData();
        cover.setUrl(storageService.getFullUrl(source.getThumbnailUrl()));
        target.setCover(cover);
        PhysicalBookData physicalBook = new PhysicalBookData();
        physicalBook.setCurrentPrice(source.getPhysicalPrice());
        target.setPhysicalBookInfo(physicalBook);
        EbookData ebook = new EbookData();
        ebook.setCurrentPrice(source.getEbookPrice());
        target.setEbookInfo(ebook);
    }
}
