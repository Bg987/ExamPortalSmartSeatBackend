package com.example.AiServicesmartSeat.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    @Value("${MONGO_URL}")
    private String url;

    @Bean
    public MongoClient mongoClient() {
        // REPLACE THE PASSWORD BELOW
        return MongoClients.create(url);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), "SmartSeatDB");
    }
}