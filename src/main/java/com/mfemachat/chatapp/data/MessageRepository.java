package com.mfemachat.chatapp.data;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.mfemachat.chatapp.models.Message;

import reactor.core.publisher.Flux;


public interface MessageRepository extends R2dbcRepository<Message, UUID> {
    Flux<Message> findBySenderId(UUID senderId);

    Flux<Message> findByReceiverId(UUID receiverId);

    Flux<Message> findByGroupId(UUID groupId);
}
