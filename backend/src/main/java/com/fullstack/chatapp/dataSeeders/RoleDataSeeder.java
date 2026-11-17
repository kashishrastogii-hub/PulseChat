package com.fullstack.chatapp.dataSeeders;

import com.fullstack.chatapp.models.ERole;
import com.fullstack.chatapp.models.Role;
import com.fullstack.chatapp.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RoleDataSeeder implements ApplicationRunner {
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (ERole name : ERole.values()) {
            if (roleRepository.findByName(name).isEmpty()) {
                roleRepository.save(Role.builder().name(name).build());
            }
        }
    }
}
