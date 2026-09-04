package com.project.souklab.dao;

import com.project.souklab.model.Artisan;
import com.project.souklab.model.ArtisanFormateurRequest;
import com.project.souklab.model.FormateurRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArtisanFormateurRequestRepository extends JpaRepository<ArtisanFormateurRequest, String> {

    Page<ArtisanFormateurRequest> findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(FormateurRequestStatus status, Pageable pageable);

    Optional<ArtisanFormateurRequest> findFirstByArtisanAndDeletedAtIsNullOrderByCreatedAtDesc(Artisan artisan);

    boolean existsByArtisanAndStatusAndDeletedAtIsNull(Artisan artisan, FormateurRequestStatus status);

    Optional<ArtisanFormateurRequest> findByIdAndDeletedAtIsNull(String id);
}
