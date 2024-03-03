package com.mfemachat.chatapp.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mfemachat.chatapp.models.Role;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public interface RoleService {
    public Mono<Role> createRole(Role role);

    public Mono<Role> getRoleById(UUID roleId);

    public Mono<Void> deleteRoleById(UUID roleId);

    public Flux<Role> findAllRoles();

    public Mono<Void> deleteAllRoles();

    public Mono<Role> getRoleByName(String roleName);
}
