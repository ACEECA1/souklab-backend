package com.project.souklab.dao;

import com.project.souklab.model.PasswordResetRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, Long> {
    Page<PasswordResetRequest> findByStatus(PasswordResetRequest.ResetStatus status, Pageable pageable);
    Optional<PasswordResetRequest> findByResetTokenAndStatus(String resetToken, PasswordResetRequest.ResetStatus status);
}
