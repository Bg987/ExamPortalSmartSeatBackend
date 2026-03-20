package com.example.AiServicesmartSeat.config;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "examTaskExecutor")
    public Executor examTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 1. Core Pool Size: Number of threads always kept alive
        executor.setCorePoolSize(10);

        // 2. Max Pool Size: Maximum threads if the queue gets full
        executor.setMaxPoolSize(50);

        // 3. Queue Capacity: How many "Sync" requests can wait in line
        // For a high-traffic exam, we set this high.
        executor.setQueueCapacity(10000);

        executor.setThreadNamePrefix("ExamSync-");
        executor.initialize();
        return executor;
    }
}
