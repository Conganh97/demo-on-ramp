package com.onramp.integration.models.dto;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO cho yêu cầu tạo đơn hàng.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
    
    @NotBlank(message = "Mã tiền pháp định không được để trống")
    @JsonProperty("fiat_currency")
    private String fiatCurrency;
    
    @NotBlank(message = "Mã tiền điện tử không được để trống")
    @JsonProperty("crypto_currency")
    private String cryptoCurrency;
    
    @JsonProperty("fiat_amount")
    private Double fiatAmount;
    
    @JsonProperty("crypto_amount")
    private Double cryptoAmount;
    
    @NotBlank(message = "Địa chỉ ví không được để trống")
    @JsonProperty("wallet_address")
    private String walletAddress;
    
    @JsonProperty("payment_method_id")
    private String paymentMethodId;
    
    @NotBlank(message = "URL chuyển hướng không được để trống")
    @JsonProperty("redirect_url")
    private String redirectUrl;
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("user_email")
    private String userEmail;
    
    @JsonProperty("user_country")
    private String userCountry;

    @JsonProperty("provider_name")
    private String providerName;
}

