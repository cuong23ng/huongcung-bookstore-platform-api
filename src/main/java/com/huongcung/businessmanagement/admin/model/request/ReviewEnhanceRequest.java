package com.huongcung.businessmanagement.admin.model.request;

import lombok.Data;

@Data
public class ReviewEnhanceRequest {
    /**
     * Type of AI enhancement:
     * - "improve": Improve and polish the existing review
     * - "expand": Expand the review significantly with more depth and analysis
     * - "shorten": Shorten and condense the review while keeping key points
     */
    private String enhancementType = "improve";
    
    /**
     * Optional: Additional instructions for AI
     */
    private String instructions;
}

