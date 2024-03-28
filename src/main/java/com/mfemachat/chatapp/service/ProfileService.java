package com.mfemachat.chatapp.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mfemachat.chatapp.models.Profile;

import reactor.core.publisher.Mono;

@Service
public interface ProfileService {
    public Mono<Profile> getProfileById(UUID id);

    public Mono<Void> deleteProfileById(UUID id);

    public Mono<Profile> updateProfileById(UUID id);

    public Mono<Profile> updateProfileByUserId(UUID userId);

    public Mono<Void> deleteProfileByUserId(UUID userId);

    public Mono<Profile> createProfile(Profile Profile);
}
