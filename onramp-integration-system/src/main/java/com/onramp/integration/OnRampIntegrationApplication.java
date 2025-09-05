package com.onramp.integration;

import com.onramp.integration.config.OnRampProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Main application class for OnRamp Integration System.
 */
@SpringBootApplication
@EnableConfigurationProperties(OnRampProperties.class)
public class OnRampIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnRampIntegrationApplication.class, args);
    }
}

