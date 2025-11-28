package com.huongcung.core.media.helper;

import com.huongcung.core.media.service.ImageService;
import com.huongcung.core.storage.service.StorageService;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

/**
 * Helper component for building full image URLs.
 * This can be injected into mappers that need URL building functionality.
 */
@Component
public class FileUrlHelper {

    private final StorageService storageService;

    public FileUrlHelper(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Builds a full URL from a relative path.
     * 
     * @param relativePath the relative path from the entity
     * @return the full URL or the relative path if service is unavailable
     */
    @Named("buildFullUrl")
    public String buildFullUrl(String relativePath) {
        if (relativePath == null || storageService == null) {
            return relativePath;
        }
        return storageService.getFullUrl(relativePath);
    }
}

