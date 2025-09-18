package com.shopedge.backend.repositories;


import com.shopedge.backend.entities.JWTToken;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface JWTTokenRepository extends JpaRepository<JWTToken, Integer> {
    
    @Query("SELECT t FROM JWTToken t WHERE t.user.userId = :userId")
    JWTToken findByUserId(@Param("userId") Integer userId);
    
    @Query("SELECT t FROM JWTToken t WHERE t.token = :token")
    Optional<JWTToken> findByToken(@Param("token") String token);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM JWTToken t WHERE t.user.userId = :userId")
    void deleteByUserId(@Param("userId") int userId);
    
}
