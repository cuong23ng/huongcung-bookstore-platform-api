package com.huongcung.businessmanagement.fulfillment.service;

import com.huongcung.businessmanagement.fulfillment.model.FulfillmentQueueDTO;
import com.huongcung.core.common.enumeration.City;
import com.huongcung.core.search.model.dto.PaginationInfo;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for fulfillment queue operations
 * Handles viewing orders that can be fulfilled from warehouses
 */
public interface FulfillmentService {
    
    /**
     * Get paginated fulfillment queue for Store Manager or Admin
     * Filters orders by status = CONFIRMED and stock availability in warehouse
     * 
     * @param city optional city filter (null for Store Manager's assigned city, or specific city for Admin)
     * @param pageable pagination parameters
     * @param fromDate optional filter by order date from
     * @param toDate optional filter by order date to
     * @param sortBy optional sort field (default: "orderDate")
     * @return PaginatedFulfillmentQueueResponse containing list of FulfillmentQueueDTO and PaginationInfo
     */
    PaginatedFulfillmentQueueResponse getFulfillmentQueue(
            City city,
            Pageable pageable,
            LocalDate fromDate,
            LocalDate toDate,
            String sortBy
    );
    
    /**
     * Response wrapper for paginated fulfillment queue
     */
    record PaginatedFulfillmentQueueResponse(
            List<FulfillmentQueueDTO> orders,
            PaginationInfo pagination
    ) {}
}

