package com.project.souklab.dao;

import com.project.souklab.model.ArtisanProfileView;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtisanProfileViewRepository extends JpaRepository<ArtisanProfileView, String> {
    boolean existsByViewerIdAndArtisanId(String viewerId, String artisanId);
}
