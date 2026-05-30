package com.pricehawl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class AlertQueuePublisher {

    private static final String ALERT_QUEUE_KEY = "alert:queue";
    private static final long MAX_QUEUE_SIZE = 1000;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final boolean redisAvailable;

    public AlertQueuePublisher(
            @Autowired(required = false) StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.redisAvailable = redisTemplate != null;
        if (!redisAvailable) {
            log.warn("Redis not available, AlertQueuePublisher will drop all events");
        }
    }

    public void publish(UUID productId, int currentPrice) {
        if (!redisAvailable) {
            log.debug("Alert event skipped (Redis unavailable): productId={}, price={}",
                productId, currentPrice);
            return;
        }
        try {
            // Kiểm tra queue có đầy không
            Long size = redisTemplate.opsForList().size(ALERT_QUEUE_KEY);
            if (size != null && size >= MAX_QUEUE_SIZE) {
                log.warn("Alert queue full ({} messages), dropping event productId={}",
                    size, productId);
                return;
            }

            String message = objectMapper.writeValueAsString(Map.of(
                "productId", productId.toString(),
                "currentPrice", currentPrice
            ));
            redisTemplate.opsForList().rightPush(ALERT_QUEUE_KEY, message);
            log.debug("Published alert event: productId={}, price={}, queueSize={}",
                productId, currentPrice, size != null ? size + 1 : 1);
        } catch (Exception e) {
            log.error("Failed to publish alert event: {}", e.getMessage());
        }
    }
}