package com.pricehawl.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class AlertQueueConsumer {

    private static final String ALERT_QUEUE_KEY = "alert:queue";
    private static final int BATCH_SIZE = 50;

    private final StringRedisTemplate redisTemplate;
    private final PriceAlertService priceAlertService;
    private final ObjectMapper objectMapper;
    private final boolean redisAvailable;

    public AlertQueueConsumer(
            @Autowired(required = false) StringRedisTemplate redisTemplate,
            PriceAlertService priceAlertService,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.priceAlertService = priceAlertService;
        this.objectMapper = objectMapper;
        this.redisAvailable = redisTemplate != null;
        if (!redisAvailable) {
            log.warn("Redis not available, AlertQueueConsumer disabled");
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void consume() {
        if (!redisAvailable) {
            return;
        }
        
        int processed = 0;
        while (processed < BATCH_SIZE) {
            String message = redisTemplate.opsForList().leftPop(ALERT_QUEUE_KEY);
            if (message == null) break;

            try {
                JsonNode node = objectMapper.readTree(message);
                UUID productId = UUID.fromString(node.get("productId").asText());
                int currentPrice = node.get("currentPrice").asInt();
                priceAlertService.checkAndTrigger(productId, currentPrice);
                processed++;
            } catch (Exception e) {
                log.error("Failed to process alert message: {} | error: {}", message, e.getMessage());
            }
        }

        if (processed > 0) {
            log.info("Alert queue processed {} messages", processed);
        }
    }
}