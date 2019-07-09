package com.github.danilkozyrev.filestorageapi.persistence;

import com.github.danilkozyrev.filestorageapi.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query("SELECT r FROM Role r WHERE r.name = 'USER'")
    Role getUserRole();

    @Query("SELECT r FROM Role r WHERE r.name = 'MANAGER'")
    Role getManagerRole();

    Optional<Role> findRoleByNameIgnoreCase(String name);

}
