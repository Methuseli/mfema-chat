package com.mfemachat.chatapp.data;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.mfemachat.chatapp.models.Message;

import reactor.core.publisher.Flux;


public interface MessageRepository extends ReactiveCrudRepository<Message, UUID> {
    Flux<Message> findBySenderId(UUID senderId);

    Flux<Message> findByReceiverId(UUID receiverId);

    Flux<Message> findByGroupId(UUID groupId);
}
