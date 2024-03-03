package com.mfemachat.chatapp.data;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.mfemachat.chatapp.models.Role;

import reactor.core.publisher.Mono;

public interface RoleRepository extends ReactiveCrudRepository<Role, UUID>{
    Mono<Role> findByName(String name);

    Mono<Void> deleteByName(String name);

    Mono<Boolean> existsByName(String name);
}
