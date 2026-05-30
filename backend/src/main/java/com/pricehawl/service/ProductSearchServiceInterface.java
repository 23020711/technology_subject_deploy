package com.pricehawl.service;

import com.pricehawl.dto.ProductSearchDTO;
import com.pricehawl.entity.Product;

import java.util.List;
import java.util.UUID;

/**
 * Interface cho ProductSearchService - cho phép có nhiều implementation.
 */
public interface ProductSearchServiceInterface {

    void syncAll();

    List<ProductSearchDTO> search(String keyword);

    List<ProductSearchDTO> searchFallback(String keyword);

    void syncOne(Product product);

    void updateBestPriceOnly(UUID productId, Integer bestPrice, String bestPlatform);

    void clearSearchCache();
}
