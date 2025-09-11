package com.shopedge.backend.repositories;

import com.shopedge.backend.entities.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    
    List<ProductImage> findByProduct_ProductId(Integer productId);
 
    @Modifying
    @Transactional
    @Query("DELETE FROM ProductImage pi WHERE pi.product.productId = :productId")
    void deleteByProductId(@Param("productId") Integer productId);
}

