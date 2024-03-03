package com.mfemachat.chatapp.dto;

import java.time.Instant;
import java.util.Set;

import com.mfemachat.chatapp.models.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserDto {
    private String username;
    private String phoneNumber;
    private String email;
    private String firstname;
    private String lastname;
    private String middlename;
    private Instant created;
    private Set<Role> roles;
}
