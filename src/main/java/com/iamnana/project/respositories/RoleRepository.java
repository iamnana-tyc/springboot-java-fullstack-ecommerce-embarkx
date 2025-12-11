package com.iamnana.project.respositories;

import com.iamnana.project.model.AppRole;
import com.iamnana.project.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(AppRole appRole);
}
