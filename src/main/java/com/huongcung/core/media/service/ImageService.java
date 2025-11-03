package com.huongcung.core.media.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    void saveImage(MultipartFile file);
    String getFullUrl(String relativePath);
}
