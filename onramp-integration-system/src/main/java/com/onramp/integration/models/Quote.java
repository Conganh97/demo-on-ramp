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
 * Mô hình dữ liệu cho báo giá.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quote {
    
    @NotNull(message = "Số tiền pháp định không được null")
    @Positive(message = "Số tiền pháp định phải lớn hơn 0")
    @JsonProperty("fiat_amount")
    private Double fiatAmount;
    
    @NotNull(message = "Số lượng tiền điện tử không được null")
    @Positive(message = "Số lượng tiền điện tử phải lớn hơn 0")
    @JsonProperty("crypto_amount")
    private Double cryptoAmount;
    
    @NotBlank(message = "Mã tiền pháp định không được để trống")
    @JsonProperty("fiat_currency")
    private String fiatCurrency;
    
    @NotBlank(message = "Mã tiền điện tử không được để trống")
    @JsonProperty("crypto_currency")
    private String cryptoCurrency;
    
    @NotNull(message = "Tỷ giá hối đoái không được null")
    @Positive(message = "Tỷ giá hối đoái phải lớn hơn 0")
    @JsonProperty("exchange_rate")
    private Double exchangeRate;
    
    @NotNull(message = "Phí giao dịch không được null")
    @JsonProperty("fee")
    private Double fee;
    
    @NotNull(message = "Tổng số tiền pháp định không được null")
    @Positive(message = "Tổng số tiền pháp định phải lớn hơn 0")
    @JsonProperty("total_fiat_amount")
    private Double totalFiatAmount;
    
    @JsonProperty("payment_method_id")
    private String paymentMethodId;
    
    @JsonProperty("valid_until")
    private LocalDateTime validUntil;
    
    @JsonProperty("provider_name")
    private String providerName;

    // Custom constructor for backward compatibility
    public Quote(Double fiatAmount, Double cryptoAmount, String fiatCurrency, String cryptoCurrency,
                 Double exchangeRate, Double fee, Double totalFiatAmount) {
        this.fiatAmount = fiatAmount;
        this.cryptoAmount = cryptoAmount;
        this.fiatCurrency = fiatCurrency;
        this.cryptoCurrency = cryptoCurrency;
        this.exchangeRate = exchangeRate;
        this.fee = fee;
        this.totalFiatAmount = totalFiatAmount;
    }
}

