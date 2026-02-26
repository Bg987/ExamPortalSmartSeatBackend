package com.example.AiServicesmartSeat.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        // The builder is automatically provided by the Spring AI Ollama starter
        return builder.build();
    }
}
