package com.rocky.cocoa.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(scanBasePackages = "com.rocky.cocoa.*")
@EntityScan(basePackages ="com.rocky.cocoa.entity")
@EnableJpaRepositories(basePackages = "com.rocky.cocoa.repository")
@Slf4j
@EnableScheduling
public class CocoaApplication {
    public static void main(String[] args) {
        SpringApplication.run(CocoaApplication.class);
    }
}
