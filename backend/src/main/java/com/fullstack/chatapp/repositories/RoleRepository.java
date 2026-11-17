package com.fullstack.chatapp.repositories;

import com.fullstack.chatapp.models.ERole;
import com.fullstack.chatapp.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}
