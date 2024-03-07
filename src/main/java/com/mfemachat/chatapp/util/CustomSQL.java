package com.mfemachat.chatapp.util;

import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CustomSQL {

  private DatabaseClient databaseClient;
  private static final String ID_USER_STRING = "userId";
  private static final String ID_ROLE_STRING = "roleId";
  private static final String USER_ID_STRING = "user_id";
  private static final String ROLE_ID_STRING = "role_id";

  public CustomSQL(DatabaseClient databaseClient) {
    this.databaseClient = databaseClient;
  }

  @SuppressWarnings("null")
  @Transactional
  public Mono<UserRole> saveUserRoles(UUID userId, UUID roleId) {
    Mono<Void> insertOperation = databaseClient
      .sql(
        "INSERT INTO users_roles (user_id, role_id) VALUES (:userId, :roleId)"
      )
      .bind(ID_USER_STRING, userId)
      .bind(ID_ROLE_STRING, roleId)
      .fetch()
      .rowsUpdated()
      .then();

    return insertOperation.then(
      databaseClient
        .sql(
          "SELECT * FROM users_roles WHERE user_id = :userId AND role_id = :roleId"
        )
        .bind(ID_ROLE_STRING, roleId)
        .bind(ID_USER_STRING, userId)
        .map((row, metadata) ->
          UserRole
            .builder()
            .userUuid(row.get(USER_ID_STRING, UUID.class))
            .roleUuid(row.get(ROLE_ID_STRING, UUID.class))
            .build()
        )
        .one()
    );
  }

  @Transactional
  public Mono<Void> deleteAllUserRoles() {
    return databaseClient
      .sql("DELETE FROM users_roles")
      .fetch()
      .rowsUpdated()
      .then();
  }

  @Transactional
  @SuppressWarnings("null")
  public Mono<Void> deleteUserRolesById(UUID userId) {
    return databaseClient
      .sql("DELETE FROM users_roles WHERE user_id = :userId")
      .bind(ID_USER_STRING, userId)
      .fetch()
      .rowsUpdated()
      .then();
  }

  @SuppressWarnings("null")
  public Flux<UserRole> getUserRolesByUserId(UUID userId) {
    return databaseClient
      .sql("SELECT * FROM users_roles WHERE user_id = :userId")
      .bind(ID_USER_STRING, userId)
      .map((row, metadata) ->
        UserRole
          .builder()
          .userUuid(row.get(USER_ID_STRING, UUID.class))
          .roleUuid(row.get(ROLE_ID_STRING, UUID.class))
          .build()
      )
      .all();
  }

  public Flux<UserRole> getAllUserRole() {
    return databaseClient
      .sql("SELECT * FROM users_roles")
      .map((row, metadata) ->
        UserRole
          .builder()
          .userUuid(row.get(USER_ID_STRING, UUID.class))
          .roleUuid(row.get(ROLE_ID_STRING, UUID.class))
          .build()
      )
      .all();
  }
}
