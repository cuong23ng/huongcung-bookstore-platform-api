package com.huongcung.core.catalog.external.service.impl;

import com.huongcung.core.catalog.enumeration.ReviewStatus;
import com.huongcung.core.catalog.external.model.AiReviewResponse;
import com.huongcung.core.catalog.external.model.SourceDTO;
import com.huongcung.core.catalog.model.dto.response.ReviewResponse;
import com.huongcung.core.catalog.model.entity.AbstractBookEntity;
import com.huongcung.core.catalog.model.entity.ReviewEntity;
import com.huongcung.core.catalog.model.entity.ReviewSource;
import com.huongcung.core.catalog.repository.AbstractBookRepository;
import com.huongcung.core.catalog.repository.ReviewRepository;
import com.huongcung.core.catalog.external.service.AiReviewService;
import com.huongcung.businessmanagement.admin.model.request.ReviewEnhanceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiReviewServiceImpl implements AiReviewService {

    private final ChatClient chatClient;
    private final AbstractBookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    @Value("classpath:prompts/review-prompt.st")
    private Resource reviewPromptResource;

    @Async("aiReviewTaskExecutor")
    public CompletableFuture<ReviewEntity> generateReviewAsync(Long bookId) {
        try {
            AbstractBookEntity book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Book not found"));

            // 1. Tạo Converter để định dạng đầu ra
            var outputConverter = new BeanOutputConverter<>(AiReviewResponse.class);

            // 2. Tạo Prompt từ Template
            PromptTemplate promptTemplate = new PromptTemplate(reviewPromptResource);
            log.info("PromptTemplate {}", promptTemplate.getTemplate());
            Prompt prompt = promptTemplate.create(Map.of(
                    "bookTitle", book.getTitle(),
                    "authorName", getAuthorNames(book),
                    "format", outputConverter.getFormat()
            ));
            log.info("Prompt: {}", prompt.getContents());

            // 3. Gọi AI
            String responseContent = chatClient.prompt(prompt).call().content();
            AiReviewResponse aiResponse = outputConverter.convert(responseContent);

            // 4. OneToOne: Update existing review or create new one
            Optional<ReviewEntity> existingReviewOpt = reviewRepository.findByBookId(bookId);
            ReviewEntity review;
            
            // Convert SourceDTO to ReviewSource
            List<ReviewSource> reviewSources = new ArrayList<>();
            if (aiResponse.getSources() != null) {
                for (SourceDTO sourceDTO : aiResponse.getSources()) {
                    ReviewSource reviewSource = new ReviewSource();
                    reviewSource.setTitle(sourceDTO.getTitle());
                    reviewSource.setUrl(sourceDTO.getUrl());
                    reviewSources.add(reviewSource);
                }
            }
            
            if (existingReviewOpt.isPresent()) {
                // Update existing review
                review = existingReviewOpt.get();
                review.setTitle(aiResponse.getTitle());
                review.setComment(aiResponse.getContent());
                review.setRating(aiResponse.getSuggestedRating());
                review.setSources(reviewSources);
                review.setIsAiGenerated(true);
                review.setStatus(ReviewStatus.DRAFT);
                log.info("Updating existing review for book {}", bookId);
            } else {
                // Create new review
                review = new ReviewEntity();
                review.setBook(book);
                review.setTitle(aiResponse.getTitle());
                review.setComment(aiResponse.getContent());
                review.setRating(aiResponse.getSuggestedRating());
                review.setSources(reviewSources);
                review.setIsAiGenerated(true);
                review.setStatus(ReviewStatus.DRAFT);
                log.info("Creating new review for book {}", bookId);
            }

            return CompletableFuture.completedFuture(reviewRepository.save(review));

        } catch (Exception e) {
            log.error("Lỗi khi tạo AI Review", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("aiReviewTaskExecutor")
    @Override
    public CompletableFuture<ReviewEntity> enhanceReviewAsync(Long bookId, ReviewEnhanceRequest request) {
        try {
            AbstractBookEntity book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Book not found"));
            
            ReviewEntity existingReview = reviewRepository.findByBookId(bookId)
                    .orElseThrow(() -> new RuntimeException("Review not found for book with id: " + bookId));
            
            if (existingReview.getComment() == null || existingReview.getComment().trim().isEmpty()) {
                throw new RuntimeException("Cannot enhance empty review. Please create a review first.");
            }

            // Build enhancement prompt based on type
            String enhancementPrompt = buildEnhancementPrompt(
                    book.getTitle(),
                    getAuthorNames(book),
                    existingReview.getComment(),
                    request.getEnhancementType(),
                    request.getInstructions()
            );

            // Call AI to enhance
            String enhancedContent = chatClient.prompt(enhancementPrompt).call().content();

            // Update review
            existingReview.setComment(enhancedContent);
            // Mark as AI enhanced (but keep original isAiGenerated flag)
            log.info("Enhancing review for book {} with type: {}", bookId, request.getEnhancementType());
            
            return CompletableFuture.completedFuture(reviewRepository.save(existingReview));

        } catch (Exception e) {
            log.error("Lỗi khi enhance AI Review", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private String buildEnhancementPrompt(String bookTitle, String authorName, String existingContent, 
                                         String enhancementType, String instructions) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a professional book reviewer. ");
        prompt.append("A review has been written for the book \"").append(bookTitle).append("\" by ").append(authorName).append(". ");
        prompt.append("Current review content:\n\n").append(existingContent).append("\n\n");
        
        switch (enhancementType != null ? enhancementType.toLowerCase() : "improve") {
            case "improve":
                prompt.append("Please improve and polish this review. Make it more engaging, professional, and well-structured. ");
                prompt.append("Keep the main points but enhance the writing quality.");
                break;
            case "expand":
                prompt.append("Please expand this review significantly. Add more depth, examples, and detailed analysis. ");
                prompt.append("Make it longer and more comprehensive while maintaining the original tone and style.");
                break;
            case "shorten":
                prompt.append("Please shorten and condense this review. Keep only the most important points and key insights. ");
                prompt.append("Make it more concise while preserving the core message and maintaining quality.");
                break;
            default:
                prompt.append("Please improve and polish this review.");
        }
        
        if (instructions != null && !instructions.trim().isEmpty()) {
            prompt.append("\n\nAdditional instructions: ").append(instructions);
        }
        
        prompt.append("\n\nReturn only the enhanced review content, without any additional explanations.");
        
        return prompt.toString();
    }

    private String getAuthorNames(AbstractBookEntity book) {
        // Logic lấy tên tác giả
        return book.getAuthors().isEmpty() ? "Unknown" : book.getAuthors().get(0).getName();
    }
}
