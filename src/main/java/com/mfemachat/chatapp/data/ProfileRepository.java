package com.mfemachat.chatapp.data;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.mfemachat.chatapp.models.Profile;

import reactor.core.publisher.Mono;

public interface ProfileRepository extends ReactiveCrudRepository<Profile, UUID> {
    Mono<Profile> findByUserId(UUID userId);
}
