package com.minero.appirfyachaq.repository;

import com.minero.appirfyachaq.entity.EntityPlant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RepositoryPlant extends JpaRepository<EntityPlant, String> {
    
    List<EntityPlant> findByStatus(String status);
}