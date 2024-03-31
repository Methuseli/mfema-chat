package com.mfemachat.chatapp.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.mfemachat.chatapp.dto.UserDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("connections")
public class Connection {

  @Id
  private UUID id;

  @NonNull
  private UUID requesterId;

  @NonNull
  private UUID receiverId;

  @NonNull
  @Column("request_status")
  private RequestStatus requestStatus;

  @Transient
  private UserDto chatConnection;
}
