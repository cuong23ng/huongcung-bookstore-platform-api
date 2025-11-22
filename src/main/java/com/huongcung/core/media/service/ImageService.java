package com.huongcung.core.media.service;

import com.huongcung.businessmanagement.admin.model.BookImageData;
import com.huongcung.businessmanagement.admin.model.ImageData;
import com.huongcung.core.media.model.entity.BookImageEntity;
import com.huongcung.core.media.model.entity.ImageEntity;
import com.huongcung.core.catalog.model.entity.AbstractBookEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface ImageService {
    /**
     * Save image from MultipartFile to S3
     * @param file the multipart file
     * @return relative path of the saved image
     */
    String saveImage(MultipartFile file, String subFolder);

    ImageEntity saveImageFromBase64(ImageData imageData, String subFolder);
    /**
     * Save image from InputStream to S3
     * @param inputStream the image input stream
     * @param fileName the filename
     * @param folderPath the folder path in S3
     * @param contentType the content type (e.g., "image/jpeg")
     * @return relative path of the saved image
     */
    String saveImageFromStream(InputStream inputStream, String fileName, String folderPath, String contentType);

    // Book Image
    BookImageEntity saveBookImageFromBase64(AbstractBookEntity book, BookImageData imageData, String subFolder);
}
