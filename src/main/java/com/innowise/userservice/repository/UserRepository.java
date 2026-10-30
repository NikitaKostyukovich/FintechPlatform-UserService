package com.innowise.userservice.repository;

import com.innowise.userservice.entity.User;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.active = :value, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :id ")
    void updateActivityAndTime(@Param("value") boolean value, @Param("id") Long id);
}
