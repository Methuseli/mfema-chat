package com.mfemachat.chatapp.util;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserRole {

  private UUID userUuid;
  private UUID roleUuid;
}
