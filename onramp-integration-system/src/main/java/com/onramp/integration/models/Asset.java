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
 * Data model for assets (cryptocurrency and fiat currency).
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
    
    @NotBlank(message = "Crypto code cannot be empty")
    @JsonProperty("crypto_code")
    @Column(name = "crypto_code", nullable = false)
    private String cryptoCode;
    
    @NotBlank(message = "Fiat code cannot be empty")
    @JsonProperty("fiat_code")
    @Column(name = "fiat_code", nullable = false)
    private String fiatCode;
    
    @NotNull(message = "Minimum amount cannot be null")
    @Positive(message = "Minimum amount must be greater than 0")
    @JsonProperty("min_amount")
    @Column(name = "min_amount", nullable = false)
    private Double minAmount;
    
    @NotNull(message = "Maximum amount cannot be null")
    @Positive(message = "Maximum amount must be greater than 0")
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

}

