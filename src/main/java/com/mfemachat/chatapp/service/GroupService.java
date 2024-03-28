package com.mfemachat.chatapp.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mfemachat.chatapp.models.Group;

import reactor.core.publisher.Mono;

@Service
public interface GroupService{
    public Mono<Group> getGroupById(UUID id);

    public Mono<Void> deleteGroupById(UUID id);

    public Mono<Group> createGroup(Group group);

    public Mono<Void> updateGroup(Group group);
}
