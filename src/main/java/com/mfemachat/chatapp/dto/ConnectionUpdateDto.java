package com.mfemachat.chatapp.dto;

import com.mfemachat.chatapp.models.RequestStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ConnectionUpdateDto {
    private RequestStatus requestStatus;
}
