package com.onramp.integration.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * Data model for orders.
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Order ID cannot be empty")
    @JsonProperty("order_id")
    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;
    
    @NotNull(message = "Order status cannot be null")
    @Enumerated(EnumType.STRING)
    @JsonProperty("status")
    @Column(name = "status", nullable = false)
    private OrderStatus status;
    
    @JsonProperty("payment_url")
    @Column(name = "payment_url", length = 1000)
    private String paymentUrl;
    
    @JsonProperty("fiat_amount")
    @Column(name = "fiat_amount")
    private Double fiatAmount;
    
    @JsonProperty("crypto_amount")
    @Column(name = "crypto_amount")
    private Double cryptoAmount;
    
    @JsonProperty("fiat_currency")
    @Column(name = "fiat_currency")
    private String fiatCurrency;
    
    @JsonProperty("crypto_currency")
    @Column(name = "crypto_currency")
    private String cryptoCurrency;
    
    @JsonProperty("wallet_address")
    @Column(name = "wallet_address")
    private String walletAddress;
    
    @JsonProperty("payment_method_id")
    @Column(name = "payment_method_id")
    private String paymentMethodId;
    
    @JsonProperty("provider_name")
    @Column(name = "provider_name")
    private String providerName;
    
    @JsonProperty("created_at")
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @JsonProperty("updated_at")
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @JsonProperty("expires_at")
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @JsonProperty("transaction_hash")
    @Column(name = "transaction_hash")
    private String transactionHash;
    
    @JsonProperty("provider_order_id")
    @Column(name = "provider_order_id")
    private String providerOrderId;

    @JsonProperty("user_id")
    @Column(name = "user_id")
    private String userId;

    @JsonProperty("redirect_url")
    @Column(name = "redirect_url", length = 1000)
    private String redirectUrl;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

