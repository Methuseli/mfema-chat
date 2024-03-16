package com.mfemachat.chatapp.models;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("profiles")
public class Profile {
    @Id
    private UUID id;

    @NonNull
    private UUID userId;

    private String description;

    private String profileImageUrl;
}
