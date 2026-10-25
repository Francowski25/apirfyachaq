package com.minero.appirfyachaq.repository;

import com.minero.appirfyachaq.entity.EntityCalculationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RepositoryCalculation extends JpaRepository<EntityCalculationHistory, String> {
    List<EntityCalculationHistory> findByIdUserOrderByCalculatedAtDesc(String idUser);
}