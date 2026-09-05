package com.project.souklab.dao;

import com.project.souklab.model.User;
import com.project.souklab.model.VerificationToken;
import com.project.souklab.model.VerificationTokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, String> {

    @Query("SELECT v FROM VerificationToken v WHERE v.user = :user AND v.type = :type AND v.usedAt IS NULL AND v.expiresAt > :now ORDER BY v.createdAt DESC LIMIT 1")
    Optional<VerificationToken> findActiveToken(
            @Param("user") User user,
            @Param("type") VerificationTokenType type,
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Query("UPDATE VerificationToken v SET v.usedAt = :now WHERE v.user = :user AND v.type = :type AND v.usedAt IS NULL")
    void invalidateActiveTokens(
            @Param("user") User user,
            @Param("type") VerificationTokenType type,
            @Param("now") LocalDateTime now
    );
}
