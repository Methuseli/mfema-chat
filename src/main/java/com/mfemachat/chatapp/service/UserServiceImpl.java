package com.mfemachat.chatapp.service;

import com.mfemachat.chatapp.data.RoleRepository;
import com.mfemachat.chatapp.data.UserRepository;
import com.mfemachat.chatapp.dto.UserUpdateDto;
import com.mfemachat.chatapp.models.Role;
import com.mfemachat.chatapp.models.User;
import com.mfemachat.chatapp.util.CustomSQL;
import com.mfemachat.chatapp.util.UserMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class UserServiceImpl implements UserService {

  private UserRepository userRepository;
  private UserMapper userMapper;
  private CustomSQL customSQL;
  private RoleRepository roleRepository;

  public UserServiceImpl(
    UserRepository userRepository,
    UserMapper userMapper,
    CustomSQL customSQL,
    RoleRepository roleRepository
  ) {
    this.userRepository = userRepository;
    this.userMapper = userMapper;
    this.customSQL = customSQL;
    this.roleRepository = roleRepository;
  }

  @Override
  @Transactional
  public Mono<User> createUser(User user) {
    return userRepository
      .existsByEmail(user.getEmail())
      .flatMap(exists -> {
        if (Boolean.TRUE.equals(exists)) {
          throw new IllegalStateException("User with email already exists");
        } else {
          return userRepository.save(user);
        }
      })
      .flatMap(savedUser -> {
        user
          .getRoles()
          .stream()
          .map(role -> customSQL.saveUserRoles(savedUser.getId(), role.getId())
          );
        return Mono.just(savedUser);
      });
  }

  @SuppressWarnings("null")
  @Override
  public Mono<User> getUserById(UUID id) {
    return userRepository.findById(id).flatMap(this::getRoles);
  }

  @SuppressWarnings("null")
  private Mono<User> getRoles(User user) {
    return customSQL
      .getUserRolesByUserId(user.getId())
      .flatMap(userRole -> {
        log.info("User role {}", userRole);
        return roleRepository.findById(userRole.getRoleUuid());
      })
      .collectList()
      .doOnNext(rolesList -> user.setRoles(new HashSet<>(rolesList)))
      .thenReturn(user);
  }

  @Override
  public Mono<User> getUserByEmail(String email) {
    return userRepository.findByEmail(email).flatMap(this::getRoles);
  }

  @SuppressWarnings("null")
  @Override
  @Transactional
  public Mono<User> updateUser(UserUpdateDto userDto, UUID id) {
    return userRepository
      .findById(id)
      .flatMap(existingUser -> {
        userMapper.updateUserFromDto(userDto, existingUser);
        if (!userDto.getRoles().isEmpty()) {
          Set<Role> newRoles = userDto.getRoles();
          customSQL
            .getUserRolesByUserId(id)
            .doOnNext(userRole -> {
              List<Role> roleComparison = newRoles
                .stream()
                .filter(role -> role.getId() == userRole.getRoleUuid())
                .toList();
              if (!roleComparison.isEmpty()) {
                newRoles.remove(roleComparison.get(0));
              }
            });

          newRoles
            .stream()
            .forEach(role -> customSQL.saveUserRoles(id, role.getId()));
        }
        return userRepository.save(existingUser);
      });
  }

  @Override
  public Flux<User> getAllUsers() {
    return userRepository.findAll();
  }

  @SuppressWarnings("null")
  @Override
  @Transactional
  public Mono<Void> deleteUserById(UUID id) {
    userRepository
      .findById(id)
      .flatMap(user -> customSQL.deleteUserRolesById(user.getId()));
    return userRepository.deleteById(id);
  }

  @Override
  @Transactional
  public Mono<Void> deleteUserByEmail(String email) {
    userRepository
      .findByEmail(email)
      .flatMap(user -> customSQL.deleteUserRolesById(user.getId()));
    return userRepository.deleteByEmail(email);
  }

  @Override
  @Transactional
  public Mono<Void> deleteAllUsers() {
    userRepository
      .findAll()
      .map(user -> customSQL.deleteUserRolesById(user.getId()));
    return userRepository.deleteAll();
  }
}
