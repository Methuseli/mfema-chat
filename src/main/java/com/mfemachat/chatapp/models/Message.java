package com.mfemachat.chatapp.models;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("messages")
public class Message {

  @Id
  private UUID id;

  @NonNull
  private String message;

  @NonNull
  private UUID senderId;

  private UUID receiverId;

  private UUID groupId;

  private MessageType messageType;

  private Instant created;
}
