package io.github.songminkyu.message.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuration for task scheduling in the message service
 * Provides scheduled task execution for DLT retry mechanisms
 */
@Configuration
public class SchedulingConfiguration {

    /**
     * Task scheduler for DLT retry operations
     * Configured with appropriate thread pool settings for retry operations
     */
    @Bean
    public TaskScheduler dltRetryTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5); // Adequate for retry operations
        scheduler.setThreadNamePrefix("dlt-retry-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        return scheduler;
    }
}