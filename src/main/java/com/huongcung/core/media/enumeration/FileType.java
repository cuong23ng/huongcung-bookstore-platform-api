package com.huongcung.core.media.enumeration;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum FileType {
    JPG("image/jpeg");

    private final String code;

    FileType(String code) {
        this.code = code;
    }

    public static FileType findFileTypeByCode(String code) {
        if (code == null) {
            return null;
        }

        return Arrays.stream(FileType.values())
                .filter(type -> code.equalsIgnoreCase(type.getCode()))
                .findFirst()
                .orElse(null);
    }
}
