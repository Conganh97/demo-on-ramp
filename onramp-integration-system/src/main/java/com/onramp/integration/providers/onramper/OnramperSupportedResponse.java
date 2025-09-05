package com.onramp.integration.providers.onramper;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * Response DTOs for Onramper API calls.
 * These classes will be updated based on actual Onramper API response format.
 */

@Data
public class OnramperSupportedResponse {
    @JsonProperty("message")
    private List<OnramperAsset> message;
    
    @Data
    public static class OnramperAsset {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("symbol")
        private String symbol;
        
        @JsonProperty("network")
        private String network;
        
        @JsonProperty("type")
        private String type; // "fiat" or "crypto"
        
        @JsonProperty("minAmount")
        private Double minAmount;
        
        @JsonProperty("maxAmount")
        private Double maxAmount;
        
        @JsonProperty("available")
        private Boolean available;
    }
}

@Data
 class OnramperQuoteResponse {
    @JsonProperty("message")
    private List<OnramperQuote> message;
    
    @Data
    public static class OnramperQuote {
        @JsonProperty("onramp")
        private String onramp;
        
        @JsonProperty("fiatAmount")
        private Double fiatAmount;
        
        @JsonProperty("cryptoAmount")
        private Double cryptoAmount;
        
        @JsonProperty("fiatCurrency")
        private String fiatCurrency;
        
        @JsonProperty("cryptoCurrency")
        private String cryptoCurrency;
        
        @JsonProperty("rate")
        private Double rate;
        
        @JsonProperty("fee")
        private Double fee;
        
        @JsonProperty("totalFiatAmount")
        private Double totalFiatAmount;
        
        @JsonProperty("paymentMethod")
        private String paymentMethod;
        
        @JsonProperty("available")
        private Boolean available;
        
        @JsonProperty("errors")
        private List<String> errors;
    }
}

@Data
 class OnramperOrderResponse {
    @JsonProperty("transactionInformation")
    private TransactionInformation transactionInformation;
    
    @Data
    public static class TransactionInformation {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("redirectType")
        private String redirectType;
        
        @JsonProperty("url")
        private String url;
        
        @JsonProperty("fiatAmount")
        private Double fiatAmount;
        
        @JsonProperty("cryptoAmount")
        private Double cryptoAmount;
        
        @JsonProperty("fiatCurrency")
        private String fiatCurrency;
        
        @JsonProperty("cryptoCurrency")
        private String cryptoCurrency;
        
        @JsonProperty("walletAddress")
        private String walletAddress;
    }
}

@Data
 class OnramperTransactionResponse {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("fiatAmount")
    private Double fiatAmount;
    
    @JsonProperty("cryptoAmount")
    private Double cryptoAmount;
    
    @JsonProperty("fiatCurrency")
    private String fiatCurrency;
    
    @JsonProperty("cryptoCurrency")
    private String cryptoCurrency;
    
    @JsonProperty("walletAddress")
    private String walletAddress;
    
    @JsonProperty("paymentMethod")
    private String paymentMethod;
    
    @JsonProperty("onramp")
    private String onramp;
    
    @JsonProperty("createdAt")
    private String createdAt;
    
    @JsonProperty("updatedAt")
    private String updatedAt;
    
    @JsonProperty("txHash")
    private String txHash;
    
    @JsonProperty("fee")
    private Double fee;
}

@Data
 class OnramperPaymentMethodsResponse {
    @JsonProperty("message")
    private List<OnramperPaymentMethod> message;
    
    @Data
    public static class OnramperPaymentMethod {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("icon")
        private String icon;
        
        @JsonProperty("minLimit")
        private Double minLimit;
        
        @JsonProperty("maxLimit")
        private Double maxLimit;
        
        @JsonProperty("processingTime")
        private String processingTime;
        
        @JsonProperty("available")
        private Boolean available;
        
        @JsonProperty("supportedCurrencies")
        private List<String> supportedCurrencies;
    }
}

@Data
 class OnramperTransactionHistoryResponse {
    @JsonProperty("message")
    private List<OnramperTransactionResponse> message;
}

