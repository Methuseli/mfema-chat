package com.mfemachat.chatapp.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.mfemachat.chatapp.models.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserUpdateDto {
    private UUID id;
    private String email;
    private String firstname;
    private String lastname;
    private String middlename;
    private Instant created;
    private Set<Role> roles;
    private String password;
    private String authProvider;
}