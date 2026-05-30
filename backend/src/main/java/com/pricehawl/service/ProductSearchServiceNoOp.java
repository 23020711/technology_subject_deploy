package com.pricehawl.service;

import com.pricehawl.dto.ProductSearchDTO;
import com.pricehawl.entity.Platform;
import com.pricehawl.entity.Product;
import com.pricehawl.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * NoOp implementation - khi Elasticsearch disabled.
 * Fallback to database search.
 */
@Service
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "false", matchIfMissing = false)
@RequiredArgsConstructor
public class ProductSearchServiceNoOp implements ProductSearchServiceInterface {

    private final ProductRepository productRepository;

    @Override
    public void syncAll() {
        // No-op: Elasticsearch not available
    }

    @Override
    public List<ProductSearchDTO> search(String keyword) {
        return searchFallback(keyword);
    }

    @Override
    public List<ProductSearchDTO> searchFallback(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String searchTerm = "%" + keyword.trim().toLowerCase() + "%";
        return productRepository.searchByKeyword(searchTerm).stream()
            .limit(50)
            .map(this::toSearchDTO)
            .collect(Collectors.toList());
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

    private ProductSearchDTO toSearchDTO(Product p) {
        // Calculate best price and platform from listings
        Integer bestPrice = null;
        String bestPlatform = null;
        if (p.getListings() != null && !p.getListings().isEmpty()) {
            bestPrice = p.getListings().stream()
                .map(l -> l.getCurrentPrice())
                .filter(price -> price != null)
                .min(Integer::compareTo)
                .orElse(null);
            if (bestPrice != null) {
                Platform platform = p.getListings().stream()
                    .filter(l -> bestPrice.equals(l.getCurrentPrice()))
                    .map(l -> l.getPlatform())
                    .findFirst()
                    .orElse(null);
                bestPlatform = platform != null ? platform.name() : null;
            }
        }
        return ProductSearchDTO.builder()
            .id(p.getId())
            .name(p.getName())
            .bestPrice(bestPrice)
            .bestPlatform(bestPlatform)
            .imageUrl(p.getImageUrl())
            .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
            .build();
    }
}
