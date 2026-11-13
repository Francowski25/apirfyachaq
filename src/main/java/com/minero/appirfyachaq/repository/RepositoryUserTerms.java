package com.minero.appirfyachaq.repository;

import com.minero.appirfyachaq.entity.EntityUserTerms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositoryUserTerms extends JpaRepository<EntityUserTerms, String> {

    Optional<EntityUserTerms> findByIdUserAndIdPlant(String idUser, String idPlant);

    List<EntityUserTerms> findByIdUser(String idUser);

    void deleteByIdUserAndIdPlant(String idUser, String idPlant);

    void deleteByIdUser(String idUser);
}