package com.helix.gpo.web_crm.access.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface RoleRepository extends JpaRepository<Role, UUID> {

    boolean existsByNameIgnoreCase(String name);

}
