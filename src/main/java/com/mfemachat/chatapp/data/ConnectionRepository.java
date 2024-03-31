package com.mfemachat.chatapp.data;

import com.mfemachat.chatapp.models.Connection;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface ConnectionRepository
  extends R2dbcRepository<Connection, UUID> {
  @Query(
    "SELECT * FROM connections " +
    "WHERE (:userId = requester_id OR :userId = receiver_id) " +
    "ORDER BY id DESC LIMIT :limit OFFSET :offset"
  )
  Flux<Connection> findByRequesterIdOrReceiver(
    UUID userId,
    int offset,
    int limit
  );
}
