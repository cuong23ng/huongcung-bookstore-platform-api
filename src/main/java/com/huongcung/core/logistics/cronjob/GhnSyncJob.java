package com.huongcung.core.logistics.cronjob;

import com.huongcung.core.logistics.enumeration.ConsignmentStatus;
import com.huongcung.core.logistics.external.ghn.dto.WebhookDTO;
import com.huongcung.core.logistics.external.ghn.service.GhnProcessService;
import com.huongcung.core.logistics.external.ghn.service.GhnService;
import com.huongcung.core.logistics.model.entity.ConsignmentEntity;
import com.huongcung.core.logistics.repository.ConsignmentRepository;
import com.huongcung.core.logistics.service.LogisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GhnSyncJob {

    private final ConsignmentRepository consignmentRepository;
    private final GhnService ghnService;
    private final GhnProcessService ghnProcessService;
    private final LogisticsService logisticsService;

    @Scheduled(fixedDelay = 900000)
    public void syncGhnStatus() {
        // 1. Cấu hình Batch Size (Mỗi lần chỉ check 20 đơn để không spam GHN)
        int batchSize = 20;

        // 2. Danh sách trạng thái cần check (Đang đi đường)
        List<ConsignmentStatus> activeStatuses = Arrays.asList(
                ConsignmentStatus.PENDING,
                ConsignmentStatus.PICKED_UP,
                ConsignmentStatus.IN_TRANSIT,
                ConsignmentStatus.OUT_FOR_DELIVERY
        );

        // 3. Lấy ra 20 đơn "đói" update nhất (Lâu chưa được check)
        Pageable limit = PageRequest.of(0, batchSize);
        List<ConsignmentEntity> consignments = consignmentRepository.findOrdersToSync(activeStatuses, limit);

        if (consignments.isEmpty()) return; // Không có gì để làm

        log.info("Đang đồng bộ trạng thái cho {} đơn hàng...", consignments.size());

        for (ConsignmentEntity consignment : consignments) {
            try {
                // 4. Gọi GHN
                String ghnStatus = ghnService.getOrderStatus(consignment.getTrackingNumber());

                // 5. Nếu có trạng thái mới và KHÁC trạng thái cũ
                if (ghnStatus != null) {
                    ConsignmentStatus newStatus = logisticsService.mapGhnStatus(ghnStatus);

                    if (newStatus != null && newStatus != consignment.getStatus()) {
                        // Update Logic (Tái sử dụng code cũ)
                        WebhookDTO dto = new WebhookDTO();
                        dto.setOrderCode(consignment.getTrackingNumber());
                        dto.setStatus(ghnStatus);
                        ghnProcessService.processUpdate(dto);
                    }
                }

                // 6. QUAN TRỌNG: Đánh dấu đã check
                // Dù status có đổi hay không, ta vẫn update thời gian để nó chìm xuống dưới danh sách
                consignment.setUpdatedAt(LocalDateTime.now());
                consignmentRepository.save(consignment);

            } catch (Exception e) {
                log.error("Lỗi sync đơn: " + consignment.getTrackingNumber(), e);
            }

            // (Optional) Sleep nhẹ 100ms để tránh DDOS GHN nếu chạy quá nhanh
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        }
    }
}
