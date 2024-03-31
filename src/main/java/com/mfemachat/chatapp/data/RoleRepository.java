package com.mfemachat.chatapp.data;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.mfemachat.chatapp.models.Role;

import reactor.core.publisher.Mono;

public interface RoleRepository extends R2dbcRepository<Role, UUID>{
    Mono<Role> findByName(String name);

    Mono<Void> deleteByName(String name);

    Mono<Boolean> existsByName(String name);
}
