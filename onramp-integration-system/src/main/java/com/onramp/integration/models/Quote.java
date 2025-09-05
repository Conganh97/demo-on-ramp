package com.onramp.integration.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * Data model for quotes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quote {
    
    @NotNull(message = "Fiat amount cannot be null")
    @Positive(message = "Fiat amount must be greater than 0")
    @JsonProperty("fiat_amount")
    private Double fiatAmount;
    
    @NotNull(message = "Crypto amount cannot be null")
    @Positive(message = "Crypto amount must be greater than 0")
    @JsonProperty("crypto_amount")
    private Double cryptoAmount;
    
    @NotBlank(message = "Fiat currency cannot be empty")
    @JsonProperty("fiat_currency")
    private String fiatCurrency;
    
    @NotBlank(message = "Crypto currency cannot be empty")
    @JsonProperty("crypto_currency")
    private String cryptoCurrency;
    
    @NotNull(message = "Exchange rate cannot be null")
    @Positive(message = "Exchange rate must be greater than 0")
    @JsonProperty("exchange_rate")
    private Double exchangeRate;
    
    @NotNull(message = "Fee cannot be null")
    @JsonProperty("fee")
    private Double fee;
    
    @NotNull(message = "Total fiat amount cannot be null")
    @Positive(message = "Total fiat amount must be greater than 0")
    @JsonProperty("total_fiat_amount")
    private Double totalFiatAmount;
    
    @JsonProperty("payment_method_id")
    private String paymentMethodId;
    
    @JsonProperty("valid_until")
    private LocalDateTime validUntil;
    
    @JsonProperty("provider_name")
    private String providerName;
}

