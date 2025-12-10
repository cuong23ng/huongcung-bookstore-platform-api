package com.huongcung.core.catalog.enumeration;

public enum ReviewStatus {
    DRAFT,      // Nháp (AI vừa tạo xong, chưa ai xem)
    PUBLISHED,  // Đã đăng (Admin đã duyệt)
    REJECTED,   // Đã từ chối
    RETRACT     // Đã thu hồi
}
