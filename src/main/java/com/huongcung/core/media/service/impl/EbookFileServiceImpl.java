package com.huongcung.core.media.service.impl;

import com.huongcung.core.media.enumeration.FileType;
import com.huongcung.core.media.model.entity.EbookFileEntity;
import com.huongcung.core.media.repository.EbookFileRepository;
import com.huongcung.core.media.service.EbookFileService;
import com.huongcung.core.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;

import static com.huongcung.core.media.constant.FolderConstants.EBOOKS;

@Service
@Slf4j
@RequiredArgsConstructor
public class EbookFileServiceImpl implements EbookFileService {

    private final StorageService storageService;
    private final EbookFileRepository ebookFileRepository;

    @Override
    public EbookFileEntity saveEbookFromStream(InputStream inputStream, String fileName, String subFolder, String contentType) {
        String folderPath = EBOOKS + "/" + subFolder;
        String relativePath = storageService.save(inputStream, fileName, folderPath, contentType);

        EbookFileEntity ebook = new EbookFileEntity();
        ebook.setFileName(fileName);
        // ebook.setFileType(FileType.findFileTypeByCode(contentType));
        ebook.setUrl(relativePath);
        ebook.setDownloadCount(0);

        // ebookFileRepository.save(ebook);

        return ebook;
    }
}
