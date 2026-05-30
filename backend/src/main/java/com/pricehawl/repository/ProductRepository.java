package com.pricehawl.repository;

import com.pricehawl.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * Lấy product kèm listings (tránh lazy load)
     */
    @EntityGraph(attributePaths = {"listings"})
    List<Product> findAllByIdIn(List<UUID> ids);

    /**
     * Fallback search (chỉ dùng khi Elasticsearch lỗi)
     */
    List<Product> findByNameContainingIgnoreCase(String keyword);

    /**
     * Search products with optional platform filter
     */
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.listings WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByKeyword(String keyword);
}