package com.minero.appirfyachaq.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.minero.appirfyachaq.entity.EntityTerms;

@Repository
public interface RepositoryTerms extends JpaRepository<EntityTerms, String>{

}
