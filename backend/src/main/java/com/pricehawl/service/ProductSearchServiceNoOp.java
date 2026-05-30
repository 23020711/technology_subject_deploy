package com.pricehawl.service;

import com.pricehawl.dto.ProductSearchDTO;
import com.pricehawl.entity.Product;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * NoOp implementation - khi Elasticsearch disabled.
 */
@Service
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "false", matchIfMissing = false)
public class ProductSearchServiceNoOp implements ProductSearchServiceInterface {

    @Override
    public void syncAll() {
        // No-op: Elasticsearch not available
    }

    @Override
    public List<ProductSearchDTO> search(String keyword) {
        // Fallback to database search
        return Collections.emptyList();
    }

    @Override
    public List<ProductSearchDTO> searchFallback(String keyword) {
        return Collections.emptyList();
    }

    @Override
    public void syncOne(Product product) {
        // No-op
    }

    @Override
    public void updateBestPriceOnly(UUID productId, Integer bestPrice, String bestPlatform) {
        // No-op
    }

    @Override
    public void clearSearchCache() {
        // No-op
    }
}
