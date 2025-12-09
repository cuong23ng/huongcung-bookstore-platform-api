package com.huongcung.core.catalog.service.impl;

import com.huongcung.core.catalog.enumeration.ReviewStatus;
import com.huongcung.core.catalog.model.dto.response.ReviewResponse;
import com.huongcung.core.catalog.model.entity.AbstractBookEntity;
import com.huongcung.core.catalog.model.entity.ReviewEntity;
import com.huongcung.core.catalog.repository.AbstractBookRepository;
import com.huongcung.core.catalog.repository.ReviewRepository;
import com.huongcung.core.catalog.service.AiReviewService;
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

import java.util.Map;
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
            var outputConverter = new BeanOutputConverter<>(ReviewResponse.class);

            // 2. Tạo Prompt từ Template
            PromptTemplate promptTemplate = new PromptTemplate(reviewPromptResource);
            log.info("PromptTemplate {}", promptTemplate.getTemplate());
            Prompt prompt = promptTemplate.create(Map.of(
                    "bookTitle", book.getTitle(),
                    "authorName", getAuthorNames(book),
                    "format", outputConverter.getFormat()
            ));
            log.info("Prompt: {}", prompt.getContents());

            // 4. Gọi AI
            String responseContent = chatClient.prompt(prompt).call().content();
            ReviewResponse aiResponse = outputConverter.convert(responseContent);

            // 5. Lưu DB
            ReviewEntity review = new ReviewEntity();
            review.setBook(book);
            review.setComment(aiResponse.getContent());
            review.setRating(aiResponse.getSuggestedRating()); // Dùng điểm AI chấm
            review.setSources(aiResponse.getSources());

            // QUAN TRỌNG: Đặt trạng thái là NHÁP để người duyệt
            review.setStatus(ReviewStatus.DRAFT);
            review.setIsAiGenerated(true);

            return CompletableFuture.completedFuture(reviewRepository.save(review));

        } catch (Exception e) {
            log.info("Lỗi khi tạo AI Review", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private String getAuthorNames(AbstractBookEntity book) {
        // Logic lấy tên tác giả
        return book.getAuthors().isEmpty() ? "Unknown" : book.getAuthors().get(0).getName();
    }
}
