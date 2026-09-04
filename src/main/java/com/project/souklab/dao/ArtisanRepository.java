package com.project.souklab.dao;

import com.project.souklab.model.Artisan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtisanRepository extends JpaRepository<Artisan, String> {
}
