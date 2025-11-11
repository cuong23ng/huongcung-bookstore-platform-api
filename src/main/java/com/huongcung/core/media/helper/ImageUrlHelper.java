package com.huongcung.core.media.helper;

import com.huongcung.core.media.service.ImageService;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

/**
 * Helper component for building full image URLs.
 * This can be injected into mappers that need URL building functionality.
 */
@Component
public class ImageUrlHelper {

    private final ImageService imageService;

    public ImageUrlHelper(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * Builds a full URL from a relative path.
     * 
     * @param relativePath the relative path from the entity
     * @return the full URL or the relative path if service is unavailable
     */
    @Named("buildFullUrl")
    public String buildFullUrl(String relativePath) {
        if (relativePath == null || imageService == null) {
            return relativePath;
        }
        return imageService.getFullUrl(relativePath);
    }
}

