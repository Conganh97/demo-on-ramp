package com.onramp.integration.core;

import com.onramp.integration.models.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Common interface for all On-ramp services.
 * Defines the contract that every On-ramp service provider must follow.
 * Uses Strategy Pattern to allow switching between different providers.
 */
public interface OnRampService {

    /**
     * Gets a list of supported assets (cryptocurrencies and fiat currencies).
     * 
     * @return CompletableFuture containing a list of supported assets
     */
    CompletableFuture<List<Asset>> getSupportedAssets();

    /**
     * Gets quote for a cryptocurrency purchase transaction.
     * Must provide either fiatAmount OR cryptoAmount.
     * 
     * @param fiatCurrency Fiat currency code (e.g., USD, VND)
     * @param cryptoCurrency Cryptocurrency code (e.g., BTC, ETH)
     * @param fiatAmount Fiat amount (optional, if known in advance)
     * @param cryptoAmount Cryptocurrency amount (optional, if known in advance)
     * @return CompletableFuture containing quote information
     * @throws IllegalArgumentException if both fiatAmount and cryptoAmount are null or both have values
     */
    CompletableFuture<Quote> getQuote(String fiatCurrency, String cryptoCurrency, 
                                     Double fiatAmount, Double cryptoAmount);

    /**
     * Creates a cryptocurrency purchase order.
     * Must provide either fiatAmount OR cryptoAmount.
     * 
     * @param fiatCurrency Fiat currency code
     * @param cryptoCurrency Cryptocurrency code
     * @param fiatAmount Fiat amount (optional)
     * @param cryptoAmount Cryptocurrency amount (optional)
     * @param walletAddress Wallet address to receive cryptocurrency
     * @param redirectUrl URL to redirect after payment completion
     * @return CompletableFuture containing created order information
     * @throws IllegalArgumentException if both fiatAmount and cryptoAmount are null or both have values
     */
    CompletableFuture<Order> createOrder(String fiatCurrency, String cryptoCurrency,
                                        Double fiatAmount, Double cryptoAmount,
                                        String walletAddress, String redirectUrl);

    /**
     * Gets status of a created order.
     * 
     * @param orderId Order ID
     * @return CompletableFuture containing order status information
     * @throws IllegalArgumentException if orderId is null or empty
     */
    CompletableFuture<Order> getOrderStatus(String orderId);

    /**
     * Gets a list of supported payment methods for a specific currency pair.
     * 
     * @param fiatCurrency Fiat currency code
     * @param cryptoCurrency Cryptocurrency code
     * @return CompletableFuture containing a list of payment methods
     */
    CompletableFuture<List<PaymentMethod>> getPaymentMethods(String fiatCurrency, String cryptoCurrency);

    /**
     * Gets a user's transaction history.
     * Note: Not all providers support this functionality via API.
     * 
     * @param userId User ID
     * @return CompletableFuture containing a list of transactions
     * @throws UnsupportedOperationException if the provider doesn't support this functionality
     */
    CompletableFuture<List<Transaction>> getTransactionHistory(String userId);

    /**
     * Gets the service provider name.
     * 
     * @return Service provider name
     */
    String getProviderName();

    /**
     * Checks if service is available.
     * 
     * @return CompletableFuture containing true if service is available, false otherwise
     */
    CompletableFuture<Boolean> isServiceAvailable();

    /**
     * Validates service configuration.
     * 
     * @return CompletableFuture containing true if the configuration is valid, false otherwise
     */
    CompletableFuture<Boolean> validateConfiguration();
}

