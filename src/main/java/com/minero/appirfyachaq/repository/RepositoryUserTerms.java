package com.minero.appirfyachaq.repository;

import com.minero.appirfyachaq.entity.EntityUserTerms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositoryUserTerms extends JpaRepository<EntityUserTerms, String> {

    Optional<EntityUserTerms> findByIdUserAndIdPlant(String idUser, String idPlant);

    Optional<EntityUserTerms> findByIdUserAndIdUserPlant(String idUser, String idUserPlant);

    List<EntityUserTerms> findByIdUser(String idUser);

    void deleteByIdUserAndIdPlant(String idUser, String idPlant);

    void deleteByIdUserAndIdUserPlant(String idUser, String idUserPlant);

    void deleteByIdUser(String idUser);
}