package com.huongcung.core.product.mapper;

import com.huongcung.core.common.enumeration.Language;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface CommonMapper {

    @Named("longToString")
    default String longToString(Long value) {
        return value == null ? null : String.valueOf(value);
    }

    @Named("stringToLong")
    default Long stringToLong(String value) {
        if (value == null || value.isBlank()) return null;
        return Long.parseLong(value);
    }

    @Named("localDateToString")
    default String localDateToString(LocalDate value) {
        return value == null ? null : value.toString();
    }

    @Named("stringToLocalDate")
    default LocalDate stringToLocalDate(String value) {
        return (value == null || value.isBlank()) ? null : LocalDate.parse(value);
    }

    @Named("localDateTimeToString")
    default String localDateTimeToString(LocalDateTime value) {
        return value == null ? null : value.toString();
    }

    @Named("stringToLocalDateTime")
    default LocalDateTime stringToLocalDateTime(String value) {
        return (value == null || value.isBlank()) ? null : LocalDateTime.parse(value);
    }

    @Named("languageToString")
    default String languageToString(Language language) {
        return language == null ? null : language.name();
    }

    @Named("stringToLanguage")
    default Language stringToLanguage(String value) {
        return (value == null || value.isBlank()) ? null : Language.valueOf(value);
    }
}
