package com.newsaggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
public class NewsAggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsAggregatorApplication.class, args);
    }
} 