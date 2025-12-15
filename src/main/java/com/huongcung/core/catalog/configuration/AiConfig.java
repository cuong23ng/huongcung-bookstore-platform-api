package com.huongcung.core.catalog.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("Bạn là một trợ lý AI hữu ích chuyên về văn học và sách.") // Cấu hình mặc định (tùy chọn)
                .build();
    }

    @Bean(name = "aiReviewTaskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);   // Số luồng cơ bản
        executor.setMaxPoolSize(10);   // Số luồng tối đa khi tải cao
        executor.setQueueCapacity(500); // Hàng đợi chờ xử lý
        executor.setThreadNamePrefix("AiReview-");
        executor.initialize();
        return executor;
    }
}
