package com.huongcung.core.logistics.enumeration;

public enum ConsignmentStatus {
    CREATED,        // Consignment created but shipping order not yet created
    PENDING,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    FAILED_DELIVERY,
    RETURNED
}
