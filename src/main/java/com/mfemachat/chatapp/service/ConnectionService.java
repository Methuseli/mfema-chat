package com.mfemachat.chatapp.service;

import java.util.UUID;

import com.mfemachat.chatapp.dto.ConnectionUpdateDto;
import com.mfemachat.chatapp.models.Connection;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ConnectionService {
    Mono<Connection> connectUsers(Connection connection);

    Flux<Connection> getConnections(UUID userId, int page, int size);

    Mono<Connection> updateConnection(UUID id, ConnectionUpdateDto connectionDto);
}
