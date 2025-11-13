package com.huongcung.core.media.service.impl;

import com.huongcung.core.configuration.S3ClientConfig;
import com.huongcung.core.media.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final S3ClientConfig s3ClientConfig;
    private final S3Client s3Client;
    
    private static final Pattern BASE64_DATA_URI_PATTERN = Pattern.compile("^data:([^;]+);base64,(.+)$");

    @Override
    public String saveImage(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.isBlank()) {
                fileName = generateFileName(file.getContentType());
            }
            
            String folderPath = "images"; // Default folder
            String contentType = file.getContentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = "image/jpeg"; // Default
            }
            
            return saveImageFromStream(file.getInputStream(), fileName, folderPath, contentType);
        } catch (IOException e) {
            log.error("Failed to read multipart file", e);
            throw new RuntimeException("Failed to save image: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String saveImageFromBase64(String base64Data, String fileName, String folderPath) {
        if (base64Data == null || base64Data.isBlank()) {
            throw new IllegalArgumentException("Base64 data cannot be null or empty");
        }
        
        // Parse Base64 data (handle data URI format: data:image/jpeg;base64,...)
        String contentType = "image/jpeg"; // Default
        String actualBase64Data = base64Data;
        
        Matcher matcher = BASE64_DATA_URI_PATTERN.matcher(base64Data.trim());
        if (matcher.matches()) {
            contentType = matcher.group(1);
            actualBase64Data = matcher.group(2);
        }
        
        // Decode Base64
        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(actualBase64Data);
        } catch (IllegalArgumentException e) {
            log.error("Invalid Base64 data", e);
            throw new IllegalArgumentException("Invalid Base64 data: " + e.getMessage(), e);
        }
        
        // Generate filename if not provided
        if (fileName == null || fileName.isBlank()) {
            fileName = generateFileName(contentType);
        }
        
        // Ensure folder path is not null
        if (folderPath == null || folderPath.isBlank()) {
            folderPath = "images";
        }
        
        // Convert bytes to InputStream
        InputStream inputStream = new ByteArrayInputStream(imageBytes);
        
        return saveImageFromStream(inputStream, fileName, folderPath, contentType);
    }
    
    @Override
    public String saveImageFromStream(InputStream inputStream, String fileName, String folderPath, String contentType) {
        if (s3ClientConfig == null || s3ClientConfig.getBucket() == null || s3ClientConfig.getBucket().isBlank()) {
            throw new IllegalStateException("S3 configuration is not properly set");
        }
        
        try {
            // Build S3 key (path)
            String key = folderPath.endsWith("/") 
                ? folderPath + fileName 
                : folderPath + "/" + fileName;
            
            // Remove leading slash if present
            if (key.startsWith("/")) {
                key = key.substring(1);
            }
            
            // Create PutObjectRequest
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3ClientConfig.getBucket())
                    .key(key)
                    .contentType(contentType != null ? contentType : "image/jpeg")
                    .build();
            
            // Read all bytes from input stream
            byte[] imageBytes = inputStream.readAllBytes();
            
            // Upload to S3
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));
            
            log.debug("Image uploaded to S3: bucket={}, key={}", s3ClientConfig.getBucket(), key);
            
            // Return relative path (without bucket name)
            return key;
            
        } catch (IOException e) {
            log.error("Failed to read image stream", e);
            throw new RuntimeException("Failed to save image: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to upload image to S3", e);
            throw new RuntimeException("Failed to upload image to S3: " + e.getMessage(), e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.warn("Failed to close input stream", e);
            }
        }
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
            // Remove protocol if present
            if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
                // Keep as is
            } else {
                endpoint = "http://" + endpoint;
            }
            
            if (!endpoint.endsWith("/")) {
                endpoint += "/";
            }
            
            return endpoint + s3ClientConfig.getBucket() + "/" + cleanPath;
        }

        // Fallback: return relative path as is if config is not available
        return relativePath;
    }
    
    /**
     * Generate a unique filename based on content type
     */
    private String generateFileName(String contentType) {
        String extension = "jpg"; // Default
        
        if (contentType != null) {
            if (contentType.contains("png")) {
                extension = "png";
            } else if (contentType.contains("gif")) {
                extension = "gif";
            } else if (contentType.contains("webp")) {
                extension = "webp";
            } else if (contentType.contains("jpeg") || contentType.contains("jpg")) {
                extension = "jpg";
            }
        }
        
        return UUID.randomUUID().toString() + "." + extension;
    }
}

