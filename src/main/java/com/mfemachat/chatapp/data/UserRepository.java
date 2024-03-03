package com.mfemachat.chatapp.data;

import com.mfemachat.chatapp.models.User;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, UUID> {
  Mono<User> findByEmail(String email);

  Mono<Void> deleteByUsername(String username);

  Mono<Void> deleteByEmail(String email);

  Mono<Boolean> existsByEmail(String email);

  Mono<Boolean> existsByUsername(String username);
}
