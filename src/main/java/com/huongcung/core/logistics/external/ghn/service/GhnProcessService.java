package com.huongcung.core.logistics.external.ghn.service;

import com.huongcung.core.logistics.external.ghn.dto.WebhookDTO;

public interface GhnProcessService {
    void processUpdate(WebhookDTO data);
}
