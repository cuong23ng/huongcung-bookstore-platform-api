package com.huongcung.core.media.service;

import com.huongcung.core.media.model.entity.EbookFileEntity;

import java.io.InputStream;

public interface EbookFileService {
    EbookFileEntity saveEbookFromStream(InputStream inputStream, String fileName, String folderPath, String contentType);
}
