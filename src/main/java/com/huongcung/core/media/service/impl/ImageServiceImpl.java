package com.huongcung.core.media.service.impl;

import com.huongcung.businessmanagement.admin.model.BookImageData;
import com.huongcung.businessmanagement.admin.model.ImageData;
import com.huongcung.core.media.enumeration.FileType;
import com.huongcung.core.media.model.entity.BookImageEntity;
import com.huongcung.core.media.model.entity.ImageEntity;
import com.huongcung.core.media.repository.BookImageRepository;
import com.huongcung.core.media.repository.ImageRepository;
import com.huongcung.core.media.service.ImageService;
import com.huongcung.core.catalog.model.entity.BookEntity;
import com.huongcung.core.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

import static com.huongcung.core.media.constant.FolderConstants.IMAGES;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final BookImageRepository bookImageRepository;
    private final StorageService storageService;

    @Override
    public String saveImage(MultipartFile file, String subFolder) {
        String folderPath = IMAGES + "/" + subFolder;
        return storageService.save(file, folderPath);
    }

    @Override
    public ImageEntity saveImageFromBase64(ImageData imageData, String subFolder) {
        if (imageData == null || !StringUtils.hasText(imageData.getBase64Data())) {
            log.warn("Image is null or Image has no Base64 Data");
            return null;
        }

        // Generate filename if not provided
        String fileName = imageData.getFileName();
        if (fileName == null || fileName.isBlank()) {
            fileName = "image_" + LocalDateTime.now() + ".jpg"; // Default filename
        }

        final String folderPath = IMAGES + "/" + subFolder;

        String relativePath = storageService.save(
                imageData.getBase64Data(), fileName, folderPath, imageData.getFileType());

        // Create ImageEntity
        ImageEntity image = new ImageEntity();
        image.setUrl(relativePath);
        image.setAltText(fileName);
        image.setFileName(fileName);
        image.setFileType(FileType.findFileTypeByCode(imageData.getFileType()));

        ImageEntity savedImage = imageRepository.save(image);

        log.info("Image uploaded successfully; imageId: {}, url: {}",
                savedImage.getId(), relativePath);
        return savedImage;
    }

    @Override
    public String saveImageFromStream(InputStream inputStream, String fileName, String subFolder, String contentType) {
        String folderPath = IMAGES + "/" + subFolder;
        return storageService.save(inputStream, fileName, folderPath, contentType);
    }

    @Override
    public BookImageEntity saveBookImageFromBase64(List<BookEntity> books, BookImageData imageData, String subFolder) {
        if (CollectionUtils.isEmpty(books)) {
            return null;
        }

        if (imageData == null || !StringUtils.hasText(imageData.getBase64Data())) {
            log.warn("Book Image is null or Image has no Base64 Data");
            return null;
        }

        // Generate filename if not provided
        String fileName = imageData.getFileName();
        if (fileName == null || fileName.isBlank()) {
            fileName = "image_" + books.get(0).getCode() + "_" + imageData.getPosition() + ".jpg"; // Default filename
        }

        final String folderPath = IMAGES + "/" + subFolder;

        String relativePath = storageService.save(imageData.getBase64Data(), fileName, folderPath, imageData.getFileType());

        // Create ImageEntity
        BookImageEntity image = new BookImageEntity();
        image.setUrl(relativePath);
        image.setAltText(fileName);
        image.setFileName(fileName);
        image.setFileType(FileType.findFileTypeByCode(imageData.getFileType()));
        image.setBooks(books);
        image.setPosition(imageData.getPosition());

        BookImageEntity savedImage = bookImageRepository.save(image);

        log.info("Book Image uploaded successfully; imageId: {}, url: {}",
                savedImage.getId(), relativePath);
        return savedImage;
    }
}

