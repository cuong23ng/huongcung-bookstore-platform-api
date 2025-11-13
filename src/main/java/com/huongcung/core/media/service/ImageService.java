package com.huongcung.core.media.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface ImageService {
    /**
     * Save image from MultipartFile to S3
     * @param file the multipart file
     * @return relative path of the saved image
     */
    String saveImage(MultipartFile file);
    
    /**
     * Save image from Base64 string to S3
     * @param base64Data Base64 encoded image data (with or without data URI prefix)
     * @param fileName the filename to use (if null, will be generated)
     * @param folderPath the folder path in S3 (e.g., "books/123")
     * @return relative path of the saved image
     */
    String saveImageFromBase64(String base64Data, String fileName, String folderPath);
    
    /**
     * Save image from InputStream to S3
     * @param inputStream the image input stream
     * @param fileName the filename
     * @param folderPath the folder path in S3
     * @param contentType the content type (e.g., "image/jpeg")
     * @return relative path of the saved image
     */
    String saveImageFromStream(InputStream inputStream, String fileName, String folderPath, String contentType);
    
    String getFullUrl(String relativePath);
}
