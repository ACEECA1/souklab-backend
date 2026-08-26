package com.project.souklab.dao;

import com.project.souklab.model.ArtisanProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtisanProfileRepository extends JpaRepository<ArtisanProfile, String> {
}
