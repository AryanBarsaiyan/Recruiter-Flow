package com.futurescope.platform.auth.repository;

import com.futurescope.platform.auth.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByNameAndScope(String name, String scope);

}

