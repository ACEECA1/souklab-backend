package com.project.souklab.dao;

import com.project.souklab.model.OAuthIdentity;
import com.project.souklab.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OAuthIdentityRepository extends JpaRepository<OAuthIdentity, String> {
    Optional<OAuthIdentity> findByProviderAndProviderUserId(String provider, String providerUserId);
    List<OAuthIdentity> findByUser(User user);
    Optional<OAuthIdentity> findByProviderAndEmail(String provider, String email);
}
