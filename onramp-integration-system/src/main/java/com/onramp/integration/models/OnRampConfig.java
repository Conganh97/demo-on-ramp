package com.onramp.integration.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.Map;

/**
 * Data model for OnRamp service provider configuration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnRampConfig {
    
    @NotBlank(message = "Provider name cannot be empty")
    @JsonProperty("provider_name")
    private String providerName;
    
    @NotBlank(message = "API key cannot be empty")
    @JsonProperty("api_key")
    private String apiKey;
    
    @JsonProperty("api_secret")
    private String apiSecret;
    
    @NotBlank(message = "Base URL cannot be empty")
    @JsonProperty("base_url")
    private String baseUrl;
    
    @JsonProperty("webhook_url")
    private String webhookUrl;
    
    @JsonProperty("is_sandbox")
    @Builder.Default
    private Boolean isSandbox = false;
    
    @NotNull(message = "Timeout cannot be null")
    @Positive(message = "Timeout must be greater than 0")
    @JsonProperty("timeout")
    @Builder.Default
    private Integer timeout = 30;
    
    @NotNull(message = "Retry attempts cannot be null")
    @JsonProperty("retry_attempts")
    @Builder.Default
    private Integer retryAttempts = 3;
    
    @JsonProperty("additional_config")
    private Map<String, Object> additionalConfig;

    @JsonProperty("enabled")
    @Builder.Default
    private Boolean enabled = true;

    @JsonProperty("priority")
    @Builder.Default
    private Integer priority = 1;

    @JsonProperty("description")
    private String description;
}

