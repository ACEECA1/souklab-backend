package com.project.souklab.dao;

import com.project.souklab.model.AccountStatus;
import com.project.souklab.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"roles"})
    @Query("SELECT u FROM User u WHERE u.email = :emailOrUsername")
    Optional<User> findByUsername(@Param("emailOrUsername") String emailOrUsername);

    default boolean existsByUsername(String username) {
        return existsByEmail(username);
    }

    Page<User> findByStatus(AccountStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.deletedAt IS NULL")
    List<User> findByRoleName(@Param("roleName") String roleName);
}
