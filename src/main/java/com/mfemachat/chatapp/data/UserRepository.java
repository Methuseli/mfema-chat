package com.mfemachat.chatapp.data;

import com.mfemachat.chatapp.models.User;
import java.util.UUID;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends R2dbcRepository<User, UUID> {
  Mono<User> findByEmail(String email);

  Mono<Void> deleteByUsername(String username);

  Mono<Void> deleteByEmail(String email);

  Mono<Boolean> existsByEmail(String email);

  Mono<Boolean> existsByUsername(String username);

  Mono<User> findByUsername(String username);
}
