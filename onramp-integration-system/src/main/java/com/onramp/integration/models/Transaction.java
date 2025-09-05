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
import java.time.LocalDateTime;

/**
 * Mô hình dữ liệu cho giao dịch.
 */
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "ID giao dịch không được để trống")
    @JsonProperty("transaction_id")
    @Column(name = "transaction_id", nullable = false, unique = true)
    private String transactionId;
    
    @JsonProperty("order_id")
    @Column(name = "order_id")
    private String orderId;
    
    @NotNull(message = "Trạng thái giao dịch không được null")
    @Enumerated(EnumType.STRING)
    @JsonProperty("status")
    @Column(name = "status", nullable = false)
    private TransactionStatus status;
    
    @NotNull(message = "Số tiền pháp định không được null")
    @Positive(message = "Số tiền pháp định phải lớn hơn 0")
    @JsonProperty("fiat_amount")
    @Column(name = "fiat_amount", nullable = false)
    private Double fiatAmount;
    
    @NotNull(message = "Số lượng tiền điện tử không được null")
    @Positive(message = "Số lượng tiền điện tử phải lớn hơn 0")
    @JsonProperty("crypto_amount")
    @Column(name = "crypto_amount", nullable = false)
    private Double cryptoAmount;
    
    @NotBlank(message = "Mã tiền pháp định không được để trống")
    @JsonProperty("fiat_currency")
    @Column(name = "fiat_currency", nullable = false)
    private String fiatCurrency;
    
    @NotBlank(message = "Mã tiền điện tử không được để trống")
    @JsonProperty("crypto_currency")
    @Column(name = "crypto_currency", nullable = false)
    private String cryptoCurrency;
    
    @NotNull(message = "Thời gian giao dịch không được null")
    @JsonProperty("timestamp")
    @Column(name = "timestamp", nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    @JsonProperty("fee")
    @Column(name = "fee")
    private Double fee;
    
    @JsonProperty("wallet_address")
    @Column(name = "wallet_address")
    private String walletAddress;
    
    @JsonProperty("transaction_hash")
    @Column(name = "transaction_hash")
    private String transactionHash;
    
    @JsonProperty("provider_name")
    @Column(name = "provider_name")
    private String providerName;
    
    @JsonProperty("payment_method_id")
    @Column(name = "payment_method_id")
    private String paymentMethodId;

    @JsonProperty("user_id")
    @Column(name = "user_id")
    private String userId;

    @JsonProperty("exchange_rate")
    @Column(name = "exchange_rate")
    private Double exchangeRate;

    @JsonProperty("provider_transaction_id")
    @Column(name = "provider_transaction_id")
    private String providerTransactionId;
}

