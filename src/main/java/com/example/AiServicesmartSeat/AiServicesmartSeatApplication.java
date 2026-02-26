package com.example.AiServicesmartSeat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication(excludeName = {
		"org.springframework.ai.model.google.genai.autoconfigure.embedding.GoogleGenAiEmbeddingConnectionAutoConfiguration",
		"org.springframework.ai.model.google.genai.autoconfigure.embedding.GoogleGenAiTextEmbeddingAutoConfiguration"
})
public class AiServicesmartSeatApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiServicesmartSeatApplication.class, args);
	}
}