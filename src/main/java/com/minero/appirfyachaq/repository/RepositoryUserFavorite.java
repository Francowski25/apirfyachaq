package com.minero.appirfyachaq.repository;

import com.minero.appirfyachaq.entity.EntityUserFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositoryUserFavorite extends JpaRepository<EntityUserFavorite, String> {

    List<EntityUserFavorite> findByIdUser(String idUser);

    boolean existsByIdUserAndIdPlant(String idUser, String idPlant);

    void deleteByIdUserAndIdPlant(String idUser, String idPlant);
}