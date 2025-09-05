# Onramper Integration Guide for Buy/Sell Crypto

## Overview

[Onramper](https://docs.onramper.com/docs/getting-started) is a leading fiat-to-crypto aggregator that provides access to 20+ onramp providers, 130+ payment methods, and 800+ crypto assets. Our integration system leverages Onramper's API to provide a seamless crypto buying and selling experience.

## Integration Features

Based on Onramper's capabilities, our system supports:

- ✅ **20+ onramp providers** including Stripe and Revolut
- ✅ **130+ payment methods** for global coverage
- ✅ **800+ crypto assets** across multiple networks
- ✅ **Industry-leading success rates** and competitive fees
- ✅ **Real-time quotes** and order management
- ✅ **Webhook support** for transaction updates
- ✅ **Singleton pattern** for optimal performance

## Quick Start Integration

### 1. Configuration Setup

First, configure your Onramper credentials in `application.yml`:

```yaml
onramp:
  cache:
    enabled: true
    max-size: 100
    ttl-minutes: 60
  
  providers:
    onramper:
      api-key: ${ONRAMPER_API_KEY:your-api-key}
      api-secret: ${ONRAMPER_API_SECRET:your-secret}
      base-url: ${ONRAMPER_BASE_URL:https://api.onramper.com}
      sandbox: true  # Set to false for production
      timeout: 30
      retry-attempts: 3
      enabled: true
      priority: 1
      description: "Onramper aggregator service"
```

### 2. Environment Variables

Set up your environment variables:

```bash
export ONRAMPER_API_KEY=your-actual-api-key
export ONRAMPER_API_SECRET=your-actual-secret
export ONRAMPER_BASE_URL=https://api.onramper.com
```

## API Integration Guide

### Core Buy/Sell Crypto APIs

#### 1. Get Supported Assets

**Endpoint**: `GET /api/onramp/onramper/assets`

```bash
curl -X GET "http://localhost:8080/api/onramp/onramper/assets" \
  -H "Accept: application/json"
```

**Response**:
```json
[
  {
    "cryptoCode": "BTC",
    "fiatCode": "USD",
    "minAmount": 10.0,
    "maxAmount": 50000.0,
    "network": "bitcoin",
    "isAvailable": true,
    "providerName": "onramper"
  }
]
```

#### 2. Get Quote for Crypto Purchase

**Endpoint**: `POST /api/onramp/onramper/quote`

```bash
curl -X POST "http://localhost:8080/api/onramp/onramper/quote" \
  -H "Content-Type: application/json" \
  -d '{
    "fiatCurrency": "USD",
    "cryptoCurrency": "BTC",
    "fiatAmount": 100.0
  }'
```

**Response**:
```json
{
  "fiatAmount": 100.0,
  "cryptoAmount": 0.002847,
  "fiatCurrency": "USD",
  "cryptoCurrency": "BTC",
  "exchangeRate": 35134.5,
  "fee": 2.5,
  "totalFiatAmount": 102.5,
  "providerName": "onramper",
  "validUntil": "2024-01-15T10:30:00"
}
```

#### 3. Create Buy Order

**Endpoint**: `POST /api/onramp/onramper/order`

```bash
curl -X POST "http://localhost:8080/api/onramp/onramper/order" \
  -H "Content-Type: application/json" \
  -d '{
    "fiatCurrency": "USD",
    "cryptoCurrency": "BTC",
    "fiatAmount": 100.0,
    "walletAddress": "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
    "redirectUrl": "https://yourapp.com/success"
  }'
```

**Response**:
```json
{
  "orderId": "onramper_12345",
  "status": "PENDING_PAYMENT",
  "paymentUrl": "https://checkout.onramper.com/tx/12345",
  "fiatAmount": 100.0,
  "cryptoAmount": 0.002847,
  "fiatCurrency": "USD",
  "cryptoCurrency": "BTC",
  "walletAddress": "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
  "providerName": "onramper",
  "createdAt": "2024-01-15T10:00:00",
  "expiresAt": "2024-01-15T10:15:00"
}
```

#### 4. Check Order Status

**Endpoint**: `GET /api/onramp/onramper/order/{orderId}`

```bash
curl -X GET "http://localhost:8080/api/onramp/onramper/order/onramper_12345" \
  -H "Accept: application/json"
```

**Response**:
```json
{
  "orderId": "onramper_12345",
  "status": "COMPLETED",
  "transactionHash": "0x1234567890abcdef...",
  "fiatAmount": 100.0,
  "cryptoAmount": 0.002847,
  "fiatCurrency": "USD",
  "cryptoCurrency": "BTC",
  "providerName": "onramper",
  "updatedAt": "2024-01-15T10:05:00"
}
```

#### 5. Get Payment Methods

**Endpoint**: `GET /api/onramp/onramper/payment-methods?fiatCurrency=USD&cryptoCurrency=BTC`

```bash
curl -X GET "http://localhost:8080/api/onramp/onramper/payment-methods?fiatCurrency=USD&cryptoCurrency=BTC" \
  -H "Accept: application/json"
```

**Response**:
```json
[
  {
    "methodId": "credit_card",
    "name": "Credit Card",
    "minLimit": 10.0,
    "maxLimit": 5000.0,
    "processingTime": "5-10 minutes",
    "isAvailable": true,
    "iconUrl": "https://cdn.onramper.com/icons/credit-card.png",
    "providerName": "onramper"
  }
]
```

## Java Integration Examples

### 1. Simple Buy Crypto Service

```java
@Service
@Slf4j
public class CryptoBuyService {
    
    @Autowired
    private OnRampServiceFactory serviceFactory;
    
    public CompletableFuture<Quote> getQuote(String fiatCurrency, String cryptoCurrency, Double amount) {
        // Uses singleton pattern - cached service instance
        OnRampService service = serviceFactory.createServiceWithDefaultConfig("onramper");
        
        return service.getQuote(fiatCurrency, cryptoCurrency, amount, null)
                .thenApply(quote -> {
                    log.info("Quote received: {} {} = {} {}", 
                        amount, fiatCurrency, quote.getCryptoAmount(), cryptoCurrency);
                    return quote;
                });
    }
    
    public CompletableFuture<Order> buysCrypto(BuyCryptoRequest request) {
        OnRampService service = serviceFactory.getCachedService("onramper");
        
        return service.createOrder(
            request.getFiatCurrency(),
            request.getCryptoCurrency(),
            request.getAmount(),
            null,
            request.getWalletAddress(),
            request.getSuccessUrl()
        );
    }
}
```

### 2. Order Tracking Service

```java
@Service
@Slf4j
public class OrderTrackingService {
    
    @Autowired
    private OnRampServiceFactory serviceFactory;
    
    @Scheduled(fixedDelay = 30000) // Check every 30 seconds
    public void trackPendingOrders() {
        OnRampService service = serviceFactory.getCachedService("onramper");
        
        // Get pending orders from database
        List<String> pendingOrderIds = getPendingOrderIds();
        
        pendingOrderIds.forEach(orderId -> {
            service.getOrderStatus(orderId)
                .thenAccept(order -> {
                    if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
                        updateOrderInDatabase(order);
                        log.info("Order {} status updated to {}", orderId, order.getStatus());
                    }
                })
                .exceptionally(error -> {
                    log.error("Error tracking order {}: {}", orderId, error.getMessage());
                    return null;
                });
        });
    }
}
```

### 3. Price Monitoring Service

```java
@Service
@Slf4j
public class PriceMonitoringService {
    
    @Autowired
    private OnRampServiceFactory serviceFactory;
    
    @Cacheable(value = "crypto-prices", key = "#fiatCurrency + '_' + #cryptoCurrency")
    public CompletableFuture<Double> getCurrentPrice(String fiatCurrency, String cryptoCurrency) {
        OnRampService service = serviceFactory.getCachedService("onramper");
        
        return service.getQuote(fiatCurrency, cryptoCurrency, 100.0, null)
                .thenApply(quote -> quote.getExchangeRate())
                .exceptionally(error -> {
                    log.warn("Error getting price for {}/{}: {}", 
                        fiatCurrency, cryptoCurrency, error.getMessage());
                    return 0.0;
                });
    }
}
```

## Cache Management & Performance

### Singleton Pattern Benefits

Our integration uses singleton pattern for optimal performance:

```java
// First call - creates and caches service
OnRampService service1 = factory.createServiceWithDefaultConfig("onramper");

// Subsequent calls - returns cached instance
OnRampService service2 = factory.createServiceWithDefaultConfig("onramper");

// service1 == service2 (same instance, better performance)
```

### Cache Management APIs

Monitor and manage service instances:

```bash
# Get cache information
curl -X GET "http://localhost:8080/api/onramp/management/cache/info"

# Warm up cache for better performance
curl -X POST "http://localhost:8080/api/onramp/management/cache/onramper/warmup"

# Clear cache if needed
curl -X DELETE "http://localhost:8080/api/onramp/management/cache"
```

## Testing & Sandbox

### Sandbox Testing

According to the [Onramper documentation](https://docs.onramper.com/docs/getting-started), you can test in sandbox mode:

```yaml
onramp:
  providers:
    onramper:
      sandbox: true  # Enable sandbox mode
      base-url: "https://api-stg.onramper.com"  # Staging URL
```

### Health Checks

Monitor service availability:

```bash
curl -X GET "http://localhost:8080/api/onramp/onramper/health"
```

## Error Handling

### Common Error Scenarios

1. **Invalid API Key**:
```json
{
  "error": "API_ERROR",
  "message": "Onramper API error: 401 - Unauthorized",
  "provider": "onramper"
}
```

2. **Unsupported Currency Pair**:
```json
{
  "error": "INVALID_RESPONSE", 
  "message": "Empty quote response from Onramper",
  "provider": "onramper"
}
```

3. **Service Unavailable**:
```json
{
  "error": "NETWORK_ERROR",
  "message": "Connection error to Onramper",
  "provider": "onramper"
}
```

## Production Considerations

### Security
- Store API keys in environment variables
- Use HTTPS for all API calls
- Validate all input parameters
- Implement rate limiting

### Performance
- Leverage singleton pattern for service instances
- Use cache warmup for better response times
- Monitor cache hit rates
- Implement circuit breakers for external API calls

### Monitoring
- Track order completion rates
- Monitor API response times
- Set up alerts for failed transactions
- Use cache management endpoints for insights

## Integration Checklist

- [ ] Configure Onramper API credentials
- [ ] Test in sandbox environment
- [ ] Implement quote retrieval
- [ ] Implement order creation
- [ ] Set up order status tracking
- [ ] Configure webhook endpoints (optional)
- [ ] Implement error handling
- [ ] Add monitoring and logging
- [ ] Performance test with cache warmup
- [ ] Deploy to production with proper security

## Complete Integration Flow

### Step-by-Step Buy Crypto Flow

1. **Get Supported Assets**
   ```bash
   GET /api/onramp/onramper/assets
   ```

2. **Get Real-time Quote**
   ```bash
   POST /api/onramp/onramper/quote
   ```

3. **Create Buy Order**
   ```bash
   POST /api/onramp/onramper/order
   ```

4. **Redirect User to Payment**
   - Use `paymentUrl` from order response
   - User completes payment on Onramper's interface

5. **Track Order Status**
   ```bash
   GET /api/onramp/onramper/order/{orderId}
   ```

6. **Handle Completion**
   - Order status changes to `COMPLETED`
   - Crypto is sent to user's wallet
   - Transaction hash available

### Example Integration Code

```java
@RestController
@RequestMapping("/api/crypto")
public class CryptoBuyController {
    
    @Autowired
    private OnRampServiceFactory factory;
    
    @PostMapping("/buy")
    public CompletableFuture<ResponseEntity<Order>> buyCrypto(@RequestBody BuyRequest request) {
        OnRampService service = factory.getCachedService("onramper");
        
        return service.createOrder(
            request.getFiatCurrency(),
            request.getCryptoCurrency(),
            request.getAmount(),
            null,
            request.getWalletAddress(),
            request.getRedirectUrl()
        ).thenApply(ResponseEntity::ok);
    }
    
    @GetMapping("/order/{orderId}")
    public CompletableFuture<ResponseEntity<Order>> getOrderStatus(@PathVariable String orderId) {
        OnRampService service = factory.getCachedService("onramper");
        
        return service.getOrderStatus(orderId)
                .thenApply(ResponseEntity::ok);
    }
}
```

This integration guide provides a complete foundation for implementing crypto buy/sell functionality using Onramper's aggregation services through our optimized singleton-pattern based system.
