package com.onramp.integration.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Mô hình dữ liệu cho tài sản (tiền điện tử và tiền pháp định).
 */
@Entity
@Table(name = "assets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Mã tiền điện tử không được để trống")
    @JsonProperty("crypto_code")
    @Column(name = "crypto_code", nullable = false)
    private String cryptoCode;
    
    @NotBlank(message = "Mã tiền pháp định không được để trống")
    @JsonProperty("fiat_code")
    @Column(name = "fiat_code", nullable = false)
    private String fiatCode;
    
    @NotNull(message = "Số lượng tối thiểu không được null")
    @Positive(message = "Số lượng tối thiểu phải lớn hơn 0")
    @JsonProperty("min_amount")
    @Column(name = "min_amount", nullable = false)
    private Double minAmount;
    
    @NotNull(message = "Số lượng tối đa không được null")
    @Positive(message = "Số lượng tối đa phải lớn hơn 0")
    @JsonProperty("max_amount")
    @Column(name = "max_amount", nullable = false)
    private Double maxAmount;
    
    @JsonProperty("network")
    @Column(name = "network")
    private String network;
    
    @JsonProperty("is_available")
    @Column(name = "is_available")
    @Builder.Default
    private Boolean isAvailable = true;

    @JsonProperty("provider_name")
    @Column(name = "provider_name")
    private String providerName;

    // Custom constructor for backward compatibility
    public Asset(String cryptoCode, String fiatCode, Double minAmount, Double maxAmount) {
        this.cryptoCode = cryptoCode;
        this.fiatCode = fiatCode;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.isAvailable = true;
    }

    public Asset(String cryptoCode, String fiatCode, Double minAmount, Double maxAmount, 
                 String network, Boolean isAvailable) {
        this.cryptoCode = cryptoCode;
        this.fiatCode = fiatCode;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.network = network;
        this.isAvailable = isAvailable;
    }
}

