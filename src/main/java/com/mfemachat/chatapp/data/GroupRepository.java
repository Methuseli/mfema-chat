package com.mfemachat.chatapp.data;

import com.mfemachat.chatapp.models.Group;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface GroupRepository extends R2dbcRepository<Group, UUID> {}
