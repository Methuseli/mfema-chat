package com.mfemachat.chatapp.service;

import org.springframework.stereotype.Service;

import com.mfemachat.chatapp.models.Message;

@Service
public interface MessageProducerService {
    public void sendPrivateMessage(Message message);

    public void sendGroupMessage(Message message);

    public void sendBroadCastMessage(Message message);
}
