package com.huongcung.businessmanagement.fulfillment.model;

import com.huongcung.core.logistics.enumeration.ConsignmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for shipping a consignment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsignmentShipRequest {

    @NotNull(message = "Status is required")
    private ConsignmentStatus status; // PICKED_UP or IN_TRANSIT
}

