package com.innowise.userservice.repository;

import com.innowise.userservice.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByUserId(Long id);
    Long countByUserIdAndActiveTrue(Long id);
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Card c SET c.active = :value, c.updatedAt = CURRENT_TIMESTAMP WHERE c.id = :id")
    void activateOrDeactivateCard(@Param("value") boolean value, @Param("id") Long id);
}
