package com.meetswps.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Background work such as push notifications runs on Spring Boot's task executor. */
@Configuration
@EnableAsync
class AsyncConfig {
}
