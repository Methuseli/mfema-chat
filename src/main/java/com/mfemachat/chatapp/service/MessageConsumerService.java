package com.mfemachat.chatapp.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.mfemachat.chatapp.models.Message;

@Service
public interface MessageConsumerService {
    @KafkaListener(topics = "private-messages-topic", groupId = "chat-consumer-group")
    public void consumePrivateMessage(Message message);

    @KafkaListener(topics = "group-messages-topic", groupId = "chat-consumer-group")
    public void consumeGroupMessage(Message message);

    @KafkaListener(topics = "broadcast-messages-topic", groupId = "chat-consumer-group")
    public void consumeBroadcastMessage(Message message);
}
