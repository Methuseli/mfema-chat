package com.mfemachat.chatapp.service;

import com.mfemachat.chatapp.data.ConnectionRepository;
import com.mfemachat.chatapp.data.UserRepository;
import com.mfemachat.chatapp.dto.ConnectionUpdateDto;
import com.mfemachat.chatapp.dto.UserDto;
import com.mfemachat.chatapp.exception.BadRequestException;
import com.mfemachat.chatapp.models.Connection;
import com.mfemachat.chatapp.models.User;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class ConnectionServiceImpl implements ConnectionService {

  private ConnectionRepository connectionRepository;
  private UserRepository userRepository;

  @SuppressWarnings("null")
  @Override
  public Mono<Connection> connectUsers(Connection connection) {
    return connectionRepository.save(connection);
  }

  @Override
  public Flux<Connection> getConnections(UUID userId, int page, int size) {
    int offset = (page - 1) * size;
    return connectionRepository
      .findByRequesterIdOrReceiver(userId, offset, size)
      .flatMap(connection -> populateUsers(connection, userId));
  }

  @SuppressWarnings("null")
  @Override
  @Transactional
  public Mono<Connection> updateConnection(
    UUID id,
    ConnectionUpdateDto connectionDto
  ) {
    return connectionRepository
      .findById(id)
      .flatMap(existingConnection -> {
        if (connectionDto.getRequestStatus() == null) {
          throw new BadRequestException("Request status not defined");
        }
        existingConnection.setRequestStatus(connectionDto.getRequestStatus());
        return connectionRepository.save(existingConnection);
      });
  }

  @SuppressWarnings("null")
  private Mono<Connection> populateUsers(Connection connection, UUID userId) {
    UUID requesterId = connection.getRequesterId();
    UUID receiverId = connection.getReceiverId();
    Mono<User> chatConnection = null;

    if (userId.equals(receiverId)) {
      chatConnection = userRepository.findById(requesterId);
    } else if (userId.equals(requesterId)) {
      chatConnection = userRepository.findById(receiverId);
    } else {
      throw new BadRequestException(
        "Something went wrong retrieving user connections"
      );
    }

    return chatConnection.flatMap(user -> {
      connection.setChatConnection(
        UserDto
          .builder()
          .username(user.getUsername())
          .email(user.getEmail())
          .firstname(user.getFirstname())
          .middlename(user.getMiddlename())
          .lastname(user.getLastname())
          .phoneNumber(user.getPhoneNumber())
          .created(user.getCreated())
          .build()
      );
      return Mono.just(connection);
    });
  }
}
