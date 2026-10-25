package com.minero.appirfyachaq.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.minero.appirfyachaq.entity.EntityTracking;

import java.util.List;

@Repository
public interface RepositoryTracking extends JpaRepository<EntityTracking, String> {
    List<EntityTracking> findByUserIdUserOrderByEntryDateDesc(String idUser);
}