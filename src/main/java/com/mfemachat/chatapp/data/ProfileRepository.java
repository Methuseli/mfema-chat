package com.mfemachat.chatapp.data;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.mfemachat.chatapp.models.Profile;

import reactor.core.publisher.Mono;

public interface ProfileRepository extends R2dbcRepository<Profile, UUID> {
    Mono<Profile> findByUserId(UUID userId);
}
