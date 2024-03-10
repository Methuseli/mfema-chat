package com.mfemachat.chatapp.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mfemachat.chatapp.dto.UserUpdateDto;
import com.mfemachat.chatapp.models.User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public interface UserService {
    public Mono<User> createUser(User user, Set<UUID> roles);

    public Mono<User> getUserById(UUID id);

    public Mono<User> getUserByEmail(String email);

    public Mono<User> updateUser(UserUpdateDto userDto, UUID id);

    public Flux<User> getAllUsers();

    public Mono<Void> deleteUserById(UUID id);

    public Mono<Void> deleteUserByEmail(String email);

    public Mono<Void> deleteAllUsers();

    public Mono<Boolean> existsByEmail(String email);

    public Mono<Boolean> existsById(UUID id);
}