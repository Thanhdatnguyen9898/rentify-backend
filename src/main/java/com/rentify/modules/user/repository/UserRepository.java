package com.rentify.modules.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rentify.modules.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>,
    JpaSpecificationExecutor<User> {
    Optional<User> findByPhoneNumber(String phoneNumber);
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    @Query("SELECT u FROM User u WHERE u.phoneNumber = :identifier OR u.email = :identifier OR u.username = :identifier")
    Optional<User> findByIdentifier(@Param("identifier") String identifier);
}