package com.project.souklab.dao;

import com.project.souklab.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByName(String name);
    List<Role> findByNameIn(Collection<String> names);
}
