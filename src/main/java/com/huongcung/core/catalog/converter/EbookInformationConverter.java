package com.huongcung.core.catalog.converter;

import com.huongcung.core.catalog.model.domain.EbookInformation;
import com.huongcung.core.catalog.model.dto.EbookInformationDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class EbookInformationConverter implements Converter<EbookInformation, EbookInformationDTO> {

    @Override
    public EbookInformationDTO convert(EbookInformation ebookInformation) {
        EbookInformationDTO ebookInformationDTO = new EbookInformationDTO();
        populate(ebookInformation, ebookInformationDTO);
        return ebookInformationDTO;
    }

    private void populate(EbookInformation source, EbookInformationDTO target) {
        target.setIsbn(source.getIsbn());
        target.setPublicationDate(source.getPublicationDate());
        target.setCurrentPrice(source.getCurrentPrice());
    }
}
