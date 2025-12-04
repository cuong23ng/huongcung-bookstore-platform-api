package com.huongcung.core.logistics.external.ghn.controller;

import com.huongcung.core.logistics.external.ghn.dto.WebhookDTO;
import com.huongcung.core.logistics.external.ghn.service.GhnProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logistics")
@RequiredArgsConstructor
public class LogisticsWebhookController {

    private final GhnProcessService ghnProcessService;

    @PostMapping("/ghn-webhook")
    public ResponseEntity<String> handleGhnWebhook(@RequestBody WebhookDTO webhookData) {
        // GHN có thể yêu cầu xác thực Token trong Header (tùy cấu hình shop)
        // String token = request.getHeader("Token"); ...

        ghnProcessService.processUpdate(webhookData);

        return ResponseEntity.ok("Received");
    }
}
