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
 * Mô hình dữ liệu cho cấu hình nhà cung cấp dịch vụ On-ramp.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnRampConfig {
    
    @NotBlank(message = "Tên nhà cung cấp dịch vụ không được để trống")
    @JsonProperty("provider_name")
    private String providerName;
    
    @NotBlank(message = "API key không được để trống")
    @JsonProperty("api_key")
    private String apiKey;
    
    @JsonProperty("api_secret")
    private String apiSecret;
    
    @NotBlank(message = "URL cơ sở của API không được để trống")
    @JsonProperty("base_url")
    private String baseUrl;
    
    @JsonProperty("webhook_url")
    private String webhookUrl;
    
    @JsonProperty("is_sandbox")
    @Builder.Default
    private Boolean isSandbox = false;
    
    @NotNull(message = "Timeout không được null")
    @Positive(message = "Timeout phải lớn hơn 0")
    @JsonProperty("timeout")
    @Builder.Default
    private Integer timeout = 30;
    
    @NotNull(message = "Số lần thử lại không được null")
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

