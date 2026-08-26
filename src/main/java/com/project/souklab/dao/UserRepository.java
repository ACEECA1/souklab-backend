package com.project.souklab.dao;

import com.project.souklab.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
    Page<User> findByStatus(User.UserStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);
}
