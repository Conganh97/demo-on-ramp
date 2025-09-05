package com.onramp.integration.models;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

/**
 * Data model for payment methods.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod {
    
    @NotBlank(message = "Payment method ID cannot be empty")
    @JsonProperty("method_id")
    private String methodId;
    
    @NotBlank(message = "Payment method name cannot be empty")
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("min_limit")
    private Double minLimit;
    
    @JsonProperty("max_limit")
    private Double maxLimit;
    
    @JsonProperty("supported_currencies")
    private List<String> supportedCurrencies;
    
    @JsonProperty("processing_time")
    private String processingTime;
    
    @JsonProperty("is_available")
    @Builder.Default
    private Boolean isAvailable = true;

    @JsonProperty("provider_name")
    private String providerName;

    @JsonProperty("icon_url")
    private String iconUrl;

    @JsonProperty("description")
    private String description;
}

