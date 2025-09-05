package com.onramp.integration.providers.onramper;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for Onramper order creation requests.
 */
@Data
@Builder
public class OnramperOrderRequest {
    
    @JsonProperty("fiatCurrency")
    private String fiatCurrency;
    
    @JsonProperty("cryptoCurrency")
    private String cryptoCurrency;
    
    @JsonProperty("fiatAmount")
    private Double fiatAmount;
    
    @JsonProperty("cryptoAmount")
    private Double cryptoAmount;
    
    @JsonProperty("walletAddress")
    private String walletAddress;
    
    @JsonProperty("redirectUrl")
    private String redirectUrl;
    
    @JsonProperty("paymentMethod")
    private String paymentMethod;
    
    @JsonProperty("uuid")
    private String uuid;
    
    @JsonProperty("clientName")
    private String clientName;
    
    @JsonProperty("country")
    private String country;
}

