package com.huongcung.core.media.service.impl;

import com.huongcung.core.configuration.S3ClientConfig;
import com.huongcung.core.media.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final S3ClientConfig s3ClientConfig;

    @Override
    public void saveImage(MultipartFile file) {
        // Implementation for saving images
        // TODO: Implement image upload logic
    }

    @Override
    public String getFullUrl(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return relativePath;
        }

        // Remove leading slash if present
        String cleanPath = relativePath.startsWith("/") 
            ? relativePath.substring(1) 
            : relativePath;

        // Build full URL: endpoint/bucket/relativePath
        if (s3ClientConfig != null 
            && s3ClientConfig.getEndpoint() != null 
            && s3ClientConfig.getBucket() != null) {
            
            String endpoint = s3ClientConfig.getEndpoint();
            if (!endpoint.endsWith("/")) {
                endpoint += "/";
            }
            
            return "http://" + endpoint + s3ClientConfig.getBucket() + "/" + cleanPath;
        }

        // Fallback: return relative path as is if config is not available
        return relativePath;
    }
}

