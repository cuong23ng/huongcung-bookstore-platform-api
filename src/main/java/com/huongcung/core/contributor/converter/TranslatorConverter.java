package com.huongcung.core.contributor.converter;

import com.huongcung.core.contributor.model.domain.Translator;
import com.huongcung.core.contributor.model.dto.TranslatorDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class TranslatorConverter implements Converter<Translator, TranslatorDTO> {
    @Override
    public TranslatorDTO convert(Translator source) {
        return null;
    }
}
