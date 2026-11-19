package com.minero.appirfyachaq.repository;

import com.minero.appirfyachaq.entity.EntityUserPlant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositoryUserPlant extends JpaRepository<EntityUserPlant, String> {

    List<EntityUserPlant> findByUser_IdUser(String idUser);

    Optional<EntityUserPlant> findByIdUserPlantAndUser_IdUser(String idUserPlant, String idUser);

    boolean existsByIdUserPlantAndUser_IdUser(String idUserPlant, String idUser);
}
