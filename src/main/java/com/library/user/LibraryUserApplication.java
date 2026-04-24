package com.library.user;

import com.library.user.infrastructure.config.JwtProperties;
import com.library.user.infrastructure.config.OutboxProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan(basePackageClasses = {JwtProperties.class, OutboxProperties.class})
public class LibraryUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryUserApplication.class, args);
    }
}
