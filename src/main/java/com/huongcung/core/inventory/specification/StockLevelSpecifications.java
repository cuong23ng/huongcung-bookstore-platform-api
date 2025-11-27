package com.huongcung.core.inventory.specification;

import com.huongcung.core.common.enumeration.City;
import com.huongcung.core.inventory.model.entity.StockLevelEntity;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * Specifications for dynamic querying of StockLevelEntity
 * Provides reusable predicates for filtering stock levels
 */
public class StockLevelSpecifications {

    /**
     * Filter by warehouse ID
     */
    public static Specification<StockLevelEntity> hasWarehouseCity(City city) {
        return (root, query, cb) ->
                city != null ? cb.equal(root.get("warehouse").get("city"), city) : null;
    }
    
    /**
     * Filter by warehouse ID
     */
    public static Specification<StockLevelEntity> hasWarehouseId(Long warehouseId) {
        return (root, query, cb) -> 
            warehouseId != null ? cb.equal(root.get("warehouse").get("id"), warehouseId) : null;
    }
    
    /**
     * Filter by book title (case-insensitive LIKE search)
     */
    public static Specification<StockLevelEntity> hasBookTitleLike(String bookTitle) {
        return (root, query, cb) -> 
            StringUtils.hasText(bookTitle) 
                ? cb.like(
                    cb.lower(root.get("book").get("abstractBook").get("title")),
                    "%" + bookTitle.toLowerCase() + "%"
                  )
                : null;
    }
    
    /**
     * Filter by availability status
     * - low_stock: quantity <= reorderLevel
     * - out_of_stock: availableQuantity (quantity - reservedQuantity) <= 0
     * - available: availableQuantity > 0
     */
    public static Specification<StockLevelEntity> hasAvailabilityStatus(String availabilityStatus) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(availabilityStatus)) {
                return null;
            }
            
            Expression<Integer> availableQuantityExpr = cb.diff(
                root.get("quantity"),
                root.get("reservedQuantity")
            );
            
            return switch (availabilityStatus.toLowerCase()) {
                case "low_stock" -> 
                    cb.lessThanOrEqualTo(root.get("quantity"), root.get("reorderLevel"));
                case "out_of_stock" -> 
                    cb.lessThanOrEqualTo(availableQuantityExpr, 0);
                case "available" -> 
                    cb.greaterThan(availableQuantityExpr, 0);
                default -> null;
            };
        };
    }
    
    /**
     * Combine all specifications into one
     */
    public static Specification<StockLevelEntity> combine(
            City city,
            Long warehouseId, 
            String bookTitle, 
            String availabilityStatus) {
        // Start with a neutral specification (always true)
        Specification<StockLevelEntity> spec = (root, query, cb) -> cb.conjunction();

        spec = spec.and(hasWarehouseCity(city));
        spec = spec.and(hasWarehouseId(warehouseId));
        spec = spec.and(hasBookTitleLike(bookTitle));
        spec = spec.and(hasAvailabilityStatus(availabilityStatus));
        
        return spec;
    }
}

