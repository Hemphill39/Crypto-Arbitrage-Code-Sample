package com.krakenarbitrage.bot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableScheduling
public class SchedulingConfig {
    private final Integer maxThreadPoolSize;
    private final Integer coreThreadPoolSize;
    private final Integer executorQueueCapacity;
    private final String executorPrefix;

    public SchedulingConfig(
            @Value("${executor-max-thread-pool-size}") Integer maxThreadPoolSize,
            @Value("${executor-core-pool-size}") Integer coreThreadPoolSize,
            @Value("${executor-queue-capacity}") Integer executorQueueCapacity,
            @Value("${executor-prefix}") String prefix
    ) {
        this.maxThreadPoolSize = maxThreadPoolSize;
        this.coreThreadPoolSize = coreThreadPoolSize;
        this.executorQueueCapacity = executorQueueCapacity;
        this.executorPrefix = prefix;
    }

    @Bean
    public Executor taskScheduler() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(coreThreadPoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(maxThreadPoolSize);
        threadPoolTaskExecutor.setQueueCapacity(executorQueueCapacity);
        threadPoolTaskExecutor.setThreadNamePrefix(executorPrefix);
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}
