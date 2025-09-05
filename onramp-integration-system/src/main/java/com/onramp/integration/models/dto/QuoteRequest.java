package com.onramp.integration.models.dto;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO for quote requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteRequest {
    
    @NotBlank(message = "Fiat currency cannot be empty")
    @JsonProperty("fiat_currency")
    private String fiatCurrency;
    
    @NotBlank(message = "Crypto currency cannot be empty")
    @JsonProperty("crypto_currency")
    private String cryptoCurrency;
    
    @JsonProperty("fiat_amount")
    private Double fiatAmount;
    
    @JsonProperty("crypto_amount")
    private Double cryptoAmount;
    
    @JsonProperty("payment_method_id")
    private String paymentMethodId;
    
    @JsonProperty("user_country")
    private String userCountry;

    @JsonProperty("provider_name")
    private String providerName;

    @JsonProperty("user_id")
    private String userId;
}

