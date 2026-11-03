package com.minero.appirfyachaq.repository;

import com.minero.appirfyachaq.entity.EntityMarketPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositoryMarketPrice extends JpaRepository<EntityMarketPrice, String> {

    Optional<EntityMarketPrice> findByMetal(String metal);

    boolean existsByMetal(String metal);
}