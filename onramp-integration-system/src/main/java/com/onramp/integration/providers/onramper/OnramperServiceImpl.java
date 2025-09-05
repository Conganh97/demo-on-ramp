package com.onramp.integration.providers.onramper;

import com.onramp.integration.exceptions.OnRampException;
import com.onramp.integration.models.*;
import com.onramp.integration.services.BaseOnRampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Concrete implementation of OnRampService for Onramper provider.
 * Uses Adapter Pattern to convert Onramper API calls to standard interface.
 */
@Service
@Slf4j
public class OnramperServiceImpl extends BaseOnRampService {

    private static final String PROVIDER_NAME = "onramper";
    private static final String PRODUCTION_BASE_URL = "https://api.onramper.com";
    private static final String STAGING_BASE_URL = "https://api-stg.onramper.com";

    @Autowired
    public OnramperServiceImpl(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    protected String determineBaseUrl(OnRampConfig config) {
        String url = Boolean.TRUE.equals(config.getIsSandbox()) ? STAGING_BASE_URL : PRODUCTION_BASE_URL;
        if (config.getBaseUrl() != null && !config.getBaseUrl().trim().isEmpty()) {
            url = config.getBaseUrl();
        }
        return url;
    }

    @Override
    protected void setupHeaders(OnRampConfig config) {
        defaultHeaders.clear();
        defaultHeaders.set("Authorization", config.getApiKey());
        defaultHeaders.set("Accept", "application/json");
        defaultHeaders.set("Content-Type", "application/json");
    }

    @Override
    protected String getHealthCheckPath() {
        return "/supported";
    }

    @Override
    public CompletableFuture<List<Asset>> getSupportedAssets() {
        validateServiceConfiguration();

        return httpClient.getAsync("/supported", OnramperSupportedResponse.class)
                .thenApply(response -> {
                    List<Asset> assets = convertToAssets(response);
                    log.info("Retrieved {} supported assets from Onramper", assets.size());
                    return assets;
                })
                .exceptionally(error -> {
                    throw handleError(error, "getSupportedAssets");
                });
    }

    @Override
    public CompletableFuture<Quote> getQuote(String fiatCurrency, String cryptoCurrency,
                                             Double fiatAmount, Double cryptoAmount) {
        validateServiceConfiguration();
        validateQuoteParameters(fiatCurrency, cryptoCurrency, fiatAmount, cryptoAmount);

        String path = String.format("/quotes/%s/%s", fiatCurrency.toLowerCase(), cryptoCurrency.toLowerCase());
        Map<String, Object> queryParams = new HashMap<>();
        if (fiatAmount != null) {
            queryParams.put("amount", fiatAmount);
        }

        return httpClient.getAsync(path, queryParams, OnramperQuoteResponse.class)
                .thenApply(response -> {
                    Quote quote = convertToQuote(response, fiatCurrency, cryptoCurrency);
                    log.info("Retrieved quote from Onramper: {} {} -> {} {}",
                            quote.getFiatAmount(), quote.getFiatCurrency(),
                            quote.getCryptoAmount(), quote.getCryptoCurrency());
                    return quote;
                })
                .exceptionally(error -> {
                    throw handleError(error, "getQuote");
                });
    }

    @Override
    public CompletableFuture<Order> createOrder(String fiatCurrency, String cryptoCurrency,
                                                Double fiatAmount, Double cryptoAmount,
                                                String walletAddress, String redirectUrl) {
        validateServiceConfiguration();
        validateOrderParameters(fiatCurrency, cryptoCurrency, fiatAmount, cryptoAmount, walletAddress, redirectUrl);

        OnramperOrderRequest request = OnramperOrderRequest.builder()
                .fiatCurrency(fiatCurrency)
                .cryptoCurrency(cryptoCurrency)
                .fiatAmount(fiatAmount)
                .cryptoAmount(cryptoAmount)
                .walletAddress(walletAddress)
                .redirectUrl(redirectUrl)
                .build();

        return httpClient.postAsync("/checkout/intent", request, OnramperOrderResponse.class)
                .thenApply(response -> {
                    Order order = convertToOrder(response);
                    log.info("Created Onramper order: {}", order.getOrderId());
                    return order;
                })
                .exceptionally(error -> {
                    throw handleError(error, "createOrder");
                });
    }

    @Override
    public CompletableFuture<Order> getOrderStatus(String orderId) {
        validateServiceConfiguration();

        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }

        String path = "/transactions/" + orderId;

        HttpHeaders customHeaders = new HttpHeaders();
        if (config.getApiSecret() != null) {
            customHeaders.set("x-onramper-secret", config.getApiSecret());
        }

        return httpClient.getAsync(path, null, customHeaders, OnramperTransactionResponse.class)
                .thenApply(response -> {
                    Order order = convertTransactionToOrder(response);
                    log.info("Retrieved Onramper order status: {} - {}",
                            order.getOrderId(), order.getStatus());
                    return order;
                })
                .exceptionally(error -> {
                    throw handleError(error, "getOrderStatus");
                });
    }

    @Override
    public CompletableFuture<List<PaymentMethod>> getPaymentMethods(String fiatCurrency, String cryptoCurrency) {
        validateServiceConfiguration();

        Map<String, Object> queryParams = Map.of(
                "fiat", fiatCurrency,
                "crypto", cryptoCurrency
        );

        return httpClient.getAsync("/payments", queryParams, OnramperPaymentMethodsResponse.class)
                .thenApply(response -> {
                    List<PaymentMethod> methods = convertToPaymentMethods(response);
                    log.info("Retrieved {} payment methods from Onramper", methods.size());
                    return methods;
                })
                .exceptionally(error -> {
                    throw handleError(error, "getPaymentMethods");
                });
    }

    @Override
    public CompletableFuture<List<Transaction>> getTransactionHistory(String userId) {
        validateServiceConfiguration();

        Map<String, Object> queryParams = Map.of("userId", userId);

        return httpClient.getAsync("/transactions", queryParams, OnramperTransactionHistoryResponse.class)
                .thenApply(response -> {
                    List<Transaction> transactions = convertToTransactions(response);
                    log.info("Retrieved {} transactions from Onramper for user {}",
                            transactions.size(), userId);
                    return transactions;
                })
                .exceptionally(error -> {
                    throw handleError(error, "getTransactionHistory");
                });
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    // Conversion methods

    private List<Asset> convertToAssets(OnramperSupportedResponse response) {
        if (response == null || response.getMessage() == null) {
            return List.of();
        }

        return response.getMessage().stream()
                .filter(asset -> asset.getAvailable() != null && asset.getAvailable())
                .map(asset -> Asset.builder()
                        .cryptoCode(asset.getSymbol())
                        .fiatCode("USD") // Default, should be determined from context
                        .minAmount(asset.getMinAmount() != null ? asset.getMinAmount() : 0.0)
                        .maxAmount(asset.getMaxAmount() != null ? asset.getMaxAmount() : Double.MAX_VALUE)
                        .network(asset.getNetwork())
                        .isAvailable(asset.getAvailable())
                        .providerName(PROVIDER_NAME)
                        .build())
                .collect(Collectors.toList());
    }

    private Quote convertToQuote(OnramperQuoteResponse response, String fiatCurrency, String cryptoCurrency) {
        if (response == null || response.getMessage() == null || response.getMessage().isEmpty()) {
            throw new OnRampException("INVALID_RESPONSE", "Empty quote response from Onramper", PROVIDER_NAME);
        }

        var quote = response.getMessage().get(0);
        return Quote.builder()
                .fiatAmount(quote.getFiatAmount())
                .cryptoAmount(quote.getCryptoAmount())
                .fiatCurrency(fiatCurrency)
                .cryptoCurrency(cryptoCurrency)
                .exchangeRate(quote.getRate())
                .fee(quote.getFee())
                .totalFiatAmount(quote.getTotalFiatAmount())
                .providerName(PROVIDER_NAME)
                .build();
    }

    private Order convertToOrder(OnramperOrderResponse response) {
        if (response == null || response.getTransactionInformation() == null) {
            throw new OnRampException("INVALID_RESPONSE", "Empty order response from Onramper", PROVIDER_NAME);
        }

        var txInfo = response.getTransactionInformation();
        return Order.builder()
                .orderId(txInfo.getId())
                .providerOrderId(txInfo.getId())
                .status(mapOnramperStatusToOrderStatus(txInfo.getStatus()))
                .paymentUrl(txInfo.getUrl())
                .fiatAmount(txInfo.getFiatAmount())
                .cryptoAmount(txInfo.getCryptoAmount())
                .fiatCurrency(txInfo.getFiatCurrency())
                .cryptoCurrency(txInfo.getCryptoCurrency())
                .walletAddress(txInfo.getWalletAddress())
                .providerName(PROVIDER_NAME)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Order convertTransactionToOrder(OnramperTransactionResponse response) {
        if (response == null) {
            throw new OnRampException("INVALID_RESPONSE", "Empty transaction response from Onramper", PROVIDER_NAME);
        }

        return Order.builder()
                .orderId(response.getId())
                .providerOrderId(response.getId())
                .status(mapOnramperStatusToOrderStatus(response.getStatus()))
                .fiatAmount(response.getFiatAmount())
                .cryptoAmount(response.getCryptoAmount())
                .fiatCurrency(response.getFiatCurrency())
                .cryptoCurrency(response.getCryptoCurrency())
                .walletAddress(response.getWalletAddress())
                .transactionHash(response.getTxHash())
                .providerName(PROVIDER_NAME)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private List<PaymentMethod> convertToPaymentMethods(OnramperPaymentMethodsResponse response) {
        if (response == null || response.getMessage() == null) {
            return List.of();
        }

        return response.getMessage().stream()
                .filter(method -> method.getAvailable() != null && method.getAvailable())
                .map(method -> PaymentMethod.builder()
                        .methodId(method.getId())
                        .name(method.getName())
                        .minLimit(method.getMinLimit())
                        .maxLimit(method.getMaxLimit())
                        .supportedCurrencies(method.getSupportedCurrencies())
                        .processingTime(method.getProcessingTime())
                        .isAvailable(method.getAvailable())
                        .iconUrl(method.getIcon())
                        .providerName(PROVIDER_NAME)
                        .build())
                .collect(Collectors.toList());
    }

    private List<Transaction> convertToTransactions(OnramperTransactionHistoryResponse response) {
        if (response == null || response.getMessage() == null) {
            return List.of();
        }

        return response.getMessage().stream()
                .map(tx -> Transaction.builder()
                        .transactionId(tx.getId())
                        .orderId(tx.getId())
                        .status(mapOnramperStatusToTransactionStatus(tx.getStatus()))
                        .fiatAmount(tx.getFiatAmount())
                        .cryptoAmount(tx.getCryptoAmount())
                        .fiatCurrency(tx.getFiatCurrency())
                        .cryptoCurrency(tx.getCryptoCurrency())
                        .fee(tx.getFee())
                        .walletAddress(tx.getWalletAddress())
                        .transactionHash(tx.getTxHash())
                        .providerName(PROVIDER_NAME)
                        .providerTransactionId(tx.getId())
                        .timestamp(LocalDateTime.now()) // Should parse from tx.getCreatedAt()
                        .build())
                .collect(Collectors.toList());
    }

    private OrderStatus mapOnramperStatusToOrderStatus(String onramperStatus) {
        if (onramperStatus == null) return OrderStatus.PENDING_PAYMENT;

        return switch (onramperStatus.toLowerCase()) {
            case "pending", "waiting" -> OrderStatus.PENDING_PAYMENT;
            case "processing" -> OrderStatus.PROCESSING;
            case "completed", "success" -> OrderStatus.COMPLETED;
            case "failed", "error" -> OrderStatus.FAILED;
            case "cancelled" -> OrderStatus.CANCELLED;
            case "expired" -> OrderStatus.EXPIRED;
            default -> OrderStatus.PENDING_PAYMENT;
        };
    }

    private TransactionStatus mapOnramperStatusToTransactionStatus(String onramperStatus) {
        if (onramperStatus == null) return TransactionStatus.PENDING;

        return switch (onramperStatus.toLowerCase()) {
            case "pending", "waiting", "processing" -> TransactionStatus.PENDING;
            case "completed", "success" -> TransactionStatus.SUCCESS;
            case "failed", "error" -> TransactionStatus.FAILED;
            case "cancelled" -> TransactionStatus.CANCELLED;
            default -> TransactionStatus.PENDING;
        };
    }
}

