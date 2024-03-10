package com.mfemachat.chatapp.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RegisterUserDto {
    private String username;
    private String password;
    private String phoneNumber;
    private String email;
    private String firstname;
    private String lastname;
    private String middlename;
    private Instant created;
    private Set<UUID> roles;
}
