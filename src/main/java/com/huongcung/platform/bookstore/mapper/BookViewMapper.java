package com.huongcung.platform.bookstore.mapper;

import com.huongcung.core.common.mapper.CommonMapper;
import com.huongcung.core.product.model.dto.AbstractBookDTO;
import com.huongcung.core.contributor.model.dto.AuthorDTO;
import com.huongcung.core.media.model.dto.BookImageDTO;
import com.huongcung.core.contributor.model.dto.PublisherDTO;
import com.huongcung.core.contributor.model.dto.TranslatorDTO;
import com.huongcung.platform.bookstore.model.AuthorData;
import com.huongcung.platform.bookstore.model.BookData;
import com.huongcung.platform.bookstore.model.BookImageData;
import com.huongcung.platform.bookstore.model.PublisherData;
import com.huongcung.platform.bookstore.model.TranslatorData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        uses = { CommonMapper.class },
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT
)
public interface BookViewMapper {

    @Mapping(target = "publicationDate", source = "publicationDate", qualifiedByName = "dateToString")
    @Mapping(target = "language", source = "language", qualifiedByName = "languageToString")
    BookData toBookData(AbstractBookDTO source);

    @Mapping(target = "birthDate", source = "birthDate", qualifiedByName = "dateToString")
    AuthorData toAuthorData(AuthorDTO source);

    @Mapping(target = "birthDate", source = "birthDate", qualifiedByName = "dateToString")
    TranslatorData toTranslatorData(TranslatorDTO source);

    PublisherData toPublisherData(PublisherDTO source);

    BookImageData toBookImageData(BookImageDTO source);
}


