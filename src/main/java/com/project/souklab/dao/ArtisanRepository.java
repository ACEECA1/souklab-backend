package com.project.souklab.dao;

import com.project.souklab.model.Artisan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtisanRepository extends JpaRepository<Artisan, String> {
}
