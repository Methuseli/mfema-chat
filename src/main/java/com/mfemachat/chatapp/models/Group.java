package com.mfemachat.chatapp.models;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import io.micrometer.common.lang.NonNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("groups")
public class Group {
    @Id
    private UUID id;

    private int groupCount;

    @NonNull
    private String groupName;

    private String groupDescription;

    @NonNull 
    private String groupImage;

    @Transient
    private Set<User> users;

    @Transient Set<User> groupAdmins;

    private Instant created;
}
