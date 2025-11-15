package com.huongcung.core.storage.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageService {
    String getFullUrl(String relativePath);

    String save(InputStream inputStream, String fileName, String folderPath, String contentType);

    String save(MultipartFile file, String folderPath);

    String save(String base64Data, String fileName, String folderPath);
}
