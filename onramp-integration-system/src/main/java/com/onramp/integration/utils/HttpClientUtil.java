package com.onramp.integration.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class for HTTP operations using RestTemplate.
 * Provides common HTTP methods with async support and error handling.
 */
@Slf4j
public class HttpClientUtil {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final HttpHeaders defaultHeaders;

    public HttpClientUtil(RestTemplate restTemplate, String baseUrl, HttpHeaders defaultHeaders) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.defaultHeaders = defaultHeaders;
    }

    /**
     * Performs an async GET request.
     */
    public <T> CompletableFuture<T> getAsync(String path, Class<T> responseType) {
        return getAsync(path, null, responseType);
    }

    /**
     * Performs an async GET request with query parameters.
     */
    public <T> CompletableFuture<T> getAsync(String path, Map<String, Object> queryParams, Class<T> responseType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl + path);
                if (queryParams != null) {
                    queryParams.forEach(uriBuilder::queryParam);
                }
                
                String url = uriBuilder.toUriString();
                HttpEntity<String> entity = new HttpEntity<>(defaultHeaders);
                
                ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
                return response.getBody();
                
            } catch (Exception e) {
                log.error("GET request failed for path: {}", path, e);
                throw new RuntimeException("HTTP GET request failed", e);
            }
        });
    }

    /**
     * Performs an async GET request with custom headers.
     */
    public <T> CompletableFuture<T> getAsync(String path, Map<String, Object> queryParams, 
                                           HttpHeaders customHeaders, Class<T> responseType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl + path);
                if (queryParams != null) {
                    queryParams.forEach(uriBuilder::queryParam);
                }
                
                String url = uriBuilder.toUriString();
                
                HttpHeaders headers = new HttpHeaders();
                headers.putAll(defaultHeaders);
                if (customHeaders != null) {
                    headers.putAll(customHeaders);
                }
                
                HttpEntity<String> entity = new HttpEntity<>(headers);
                
                ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
                return response.getBody();
                
            } catch (Exception e) {
                log.error("GET request failed for path: {}", path, e);
                throw new RuntimeException("HTTP GET request failed", e);
            }
        });
    }

    /**
     * Performs an async POST request.
     */
    public <T, R> CompletableFuture<R> postAsync(String path, T requestBody, Class<R> responseType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = baseUrl + path;
                HttpEntity<T> entity = new HttpEntity<>(requestBody, defaultHeaders);
                
                ResponseEntity<R> response = restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
                return response.getBody();
                
            } catch (Exception e) {
                log.error("POST request failed for path: {}", path, e);
                throw new RuntimeException("HTTP POST request failed", e);
            }
        });
    }

    /**
     * Performs a health check on the base URL.
     */
    public CompletableFuture<Boolean> healthCheck(String healthPath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = baseUrl + healthPath;
                HttpEntity<String> entity = new HttpEntity<>(defaultHeaders);
                
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                return response.getStatusCode().is2xxSuccessful();
                
            } catch (Exception e) {
                log.debug("Health check failed for path: {}", healthPath, e);
                return false;
            }
        });
    }
}
