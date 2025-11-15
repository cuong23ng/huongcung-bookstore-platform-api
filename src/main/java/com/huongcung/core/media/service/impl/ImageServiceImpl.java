package com.huongcung.core.media.service.impl;

import com.huongcung.core.media.service.ImageService;
import com.huongcung.core.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

import static com.huongcung.core.media.constant.Constants.IMAGES_FOLDER;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final StorageService storageService;

    @Override
    public String saveImage(MultipartFile file, String subFolder) {
        String folderPath = IMAGES_FOLDER + "/" + subFolder;
        return storageService.save(file, folderPath);
    }
    
    @Override
    public String saveImageFromBase64(String base64Data, String fileName, String subFolder) {
        String folderPath = IMAGES_FOLDER + "/" + subFolder;
        return storageService.save(base64Data, fileName, folderPath);
    }
    
    @Override
    public String saveImageFromStream(InputStream inputStream, String fileName, String subFolder, String contentType) {
        String folderPath = IMAGES_FOLDER + "/" + subFolder;
        return storageService.save(inputStream, fileName, folderPath, contentType);
    }
}

