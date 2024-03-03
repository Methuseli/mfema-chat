package com.mfemachat.chatapp.models;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("users")
public class User {

  @Id
  private UUID id;

  private String username;

  @NonNull
  private String email;

  @NonNull
  private String password;

  @NonNull
  private String firstname;

  private String middlename;

  @NonNull
  private String lastname;

  private Instant created;

  @Transient
  private Set<Role> roles;

  private String authProvider;
}
