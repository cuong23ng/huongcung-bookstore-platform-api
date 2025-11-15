package com.huongcung.core.storage.service.impl;

import com.huongcung.core.storage.configuration.S3ClientConfig;
import com.huongcung.core.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageServiceImpl implements StorageService {

    private final S3ClientConfig s3ClientConfig;
    private final S3Client s3Client;

    private static final Pattern BASE64_DATA_URI_PATTERN = Pattern.compile("^data:([^;]+);base64,(.+)$");

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

            if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
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

    @Override
    public String save(InputStream inputStream, String fileName, String folderPath, String contentType) {
        if (s3ClientConfig == null || s3ClientConfig.getBucket() == null || s3ClientConfig.getBucket().isBlank()) {
            throw new IllegalStateException("S3 configuration is not properly set");
        }

        if (ObjectUtils.isEmpty(inputStream)) {
            throw new IllegalArgumentException("Illegal file stream");
        }

        if (!StringUtils.hasText(folderPath)) {
            throw new IllegalArgumentException("Missing folder path");
        }

        if (!StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("Missing file name");
        }

        if (!StringUtils.hasText(contentType)) {
            throw new IllegalArgumentException("Missing content type");
        }

        try {
            // Build path
            String key = buildS3Key(fileName, folderPath);

            // Create PutObjectRequest
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3ClientConfig.getBucket())
                    .key(key)
                    .contentType(contentType)
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
        }   catch (Exception e) {
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
    public String save(MultipartFile file, String folderPath) {
        try {
            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();

            return save(file.getInputStream(), fileName, folderPath, contentType);
        } catch (IOException e) {
            log.error("Failed to read multipart file", e);
            throw new RuntimeException("Failed to save image: " + e.getMessage(), e);
        }
    }

    @Override
    public String save(String base64Data, String fileName, String folderPath) {
        if (base64Data == null || base64Data.isBlank()) {
            throw new IllegalArgumentException("Base64 data cannot be null or empty");
        }

        // Parse Base64 data (handle data URI format: data:image/jpeg;base64,...)
        String contentType = "";
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

        // Convert bytes to InputStream
        InputStream inputStream = new ByteArrayInputStream(imageBytes);

        return save(inputStream, fileName, folderPath, contentType);
    }

    private String buildS3Key(String fileName, String folderPath) {
        String key = folderPath.endsWith("/")
                ? folderPath + fileName
                : folderPath + "/" + fileName;

        // Remove leading slash if present
        if (key.startsWith("/")) {
            key = key.substring(1);
        }

        return key;
    }
}
