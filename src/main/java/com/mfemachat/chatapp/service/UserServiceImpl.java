package com.mfemachat.chatapp.service;

import com.mfemachat.chatapp.data.RoleRepository;
import com.mfemachat.chatapp.data.UserRepository;
import com.mfemachat.chatapp.dto.RegisterUserDto;
import com.mfemachat.chatapp.dto.UserUpdateDto;
import com.mfemachat.chatapp.exception.ConflictingRequestException;
import com.mfemachat.chatapp.exception.NotFoundException;
import com.mfemachat.chatapp.models.Role;
import com.mfemachat.chatapp.models.User;
import com.mfemachat.chatapp.util.CustomSQL;
import com.mfemachat.chatapp.util.UserMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {

  private UserRepository userRepository;
  private UserMapper userMapper;
  private CustomSQL customSQL;
  private RoleRepository roleRepository;
  private PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public Mono<User> createUser(RegisterUserDto userDto) {
    return userRepository
      .existsByEmail(userDto.getEmail())
      .flatMap(exists -> {
        if (exists) {
          return Mono.error(
            new ConflictingRequestException("User with email already exists")
          );
        } else {
          return saveUser(userDto);
        }
      });
  }

  @SuppressWarnings("null")
  private Mono<User> saveUser(RegisterUserDto userDto) {
    User user = User
      .builder()
      .username(userDto.getUsername())
      .password(passwordEncoder.encode(userDto.getPassword()))
      .email(userDto.getEmail())
      .firstname(userDto.getFirstname())
      .middlename(userDto.getMiddlename())
      .lastname(userDto.getLastname())
      .build();

    return userRepository
      .save(user)
      .flatMap(savedUser -> saveRoles(savedUser, userDto.getRoles()))
      .thenReturn(user);
  }

  @SuppressWarnings("null")
  private Mono<Void> saveRoles(User user, Set<UUID> roleIds) {
    if (roleIds == null || roleIds.isEmpty()) {
      return roleRepository
        .findByName("USER")
        .flatMap(role -> customSQL.saveUserRoles(user.getId(), role.getId()))
        .then();
    } else {
      return Flux
        .fromIterable(roleIds)
        .flatMap(roleId -> roleRepository.existsById(roleId))
        .collectList()
        .flatMap(existsList -> {
          if (existsList.contains(false)) {
            return Mono.error(
              new NotFoundException("One or more roles do not exist")
            );
          } else {
            return Flux
              .fromIterable(roleIds)
              .flatMap(roleId -> customSQL.saveUserRoles(user.getId(), roleId))
              .then();
          }
        });
    }
  }

  // @SuppressWarnings("null")
  // @Transactional
  // public Mono<User> createUser(User user, Set<UUID> roles) {
  //   if (roles == null || roles.isEmpty()) {
  //     roleRepository.findByName("USER").map(role -> roles.add(role.getId()));
  //   } else {
  //     roles
  //       .stream()
  //       .forEach(role ->
  //         roleRepository
  //           .existsById(role)
  //           .map(exists -> {
  //             if (Boolean.FALSE.equals(exists)) {
  //               throw new NotFoundException(
  //                 "Role with id " + role + " does not exist"
  //               );
  //             }
  //             return Mono.empty();
  //           })
  //       );
  //   }

  //   return userRepository
  //     .existsByEmail(user.getEmail())
  //     .flatMap(exists -> {
  //       if (Boolean.TRUE.equals(exists)) {
  //         throw new ConflictingRequestException("User with email already exists");
  //       } else {
  //         return userRepository.save(user);
  //       }
  //     })
  //     .flatMap(savedUser -> {
  //       roles
  //         .stream()
  //         .map(role ->
  //           customSQL
  //             .saveUserRoles(savedUser.getId(), role)
  //             .doOnError(error ->
  //               log.error("Failed to save user roles {}", error)
  //             )
  //         );
  //       return Mono.just(savedUser);
  //     });
  // }

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

  @Override
  public Mono<Boolean> existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  @SuppressWarnings("null")
  @Override
  public Mono<Boolean> existsById(UUID id) {
    return userRepository.existsById(id);
  }
}
