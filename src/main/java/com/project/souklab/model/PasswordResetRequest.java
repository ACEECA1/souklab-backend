package com.project.souklab.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_requests")
@Getter
@Setter
public class PasswordResetRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResetStatus status = ResetStatus.PENDING;

    @Column(unique = true)
    private String resetToken; 

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum ResetStatus {
        PENDING, APPROVED, REJECTED, CONSUMED
    }
}
