package com.minero.appirfyachaq.repository;

import com.minero.appirfyachaq.entity.EntitySubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RepositorySubscription extends JpaRepository<EntitySubscription, String> {
    
    Optional<EntitySubscription> findByUserIdUserAndStatus(String idUser, String status);
    
    Optional<EntitySubscription> findByGooglePurchaseToken(String googlePurchaseToken);
}