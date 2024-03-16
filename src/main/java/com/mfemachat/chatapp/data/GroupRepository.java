package com.mfemachat.chatapp.data;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.mfemachat.chatapp.models.Group;

public interface GroupRepository extends ReactiveCrudRepository<Group, UUID>{
    
}
