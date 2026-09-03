package com.project.souklab.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "artisan_formateur_requests", indexes = {
        @Index(name = "idx_formateur_req_artisan", columnList = "artisan_id"),
        @Index(name = "idx_formateur_req_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtisanFormateurRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artisan_id", nullable = false)
    private Artisan artisan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private FormateurRequestStatus status;

    @Column(columnDefinition = "TEXT")
    private String motivation;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @Column(name = "can_reapply", nullable = false)
    @Builder.Default
    private boolean canReapply = true;

    @Column(name = "cooldown_until")
    private LocalDateTime cooldownUntil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by")
    private User decidedBy;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;
}
