package com.huongcung.core.catalog.converter;

import com.huongcung.core.catalog.model.domain.PhysicalBookInformation;
import com.huongcung.core.catalog.model.dto.PhysicalBookInformationDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class PhysicalBookInformationConverter implements Converter<PhysicalBookInformation, PhysicalBookInformationDTO> {

    @Override
    public PhysicalBookInformationDTO convert(PhysicalBookInformation physicalBookInformation) {
        PhysicalBookInformationDTO physicalBookInformationDTO = new PhysicalBookInformationDTO();
        populate(physicalBookInformation, physicalBookInformationDTO);
        return physicalBookInformationDTO;
    }

    private void populate(PhysicalBookInformation source, PhysicalBookInformationDTO target) {
        target.setIsbn(source.getIsbn());
        target.setPublicationDate(source.getPublicationDate());
        target.setCurrentPrice(source.getCurrentPrice());
        target.setCoverType(source.getCoverType().name());
        target.setLengthCm(source.getLengthCm());
        target.setHeightCm(source.getHeightCm());
        target.setWidthCm(source.getWidthCm());
        target.setWeightGrams(source.getWeightGrams());
    }
}
