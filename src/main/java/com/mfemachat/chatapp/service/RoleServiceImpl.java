package com.mfemachat.chatapp.service;

import com.mfemachat.chatapp.data.RoleRepository;
import com.mfemachat.chatapp.models.Role;

import lombok.AllArgsConstructor;

import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class RoleServiceImpl implements RoleService {

  private RoleRepository roleRepository;

  @Override
  @Transactional
  public Mono<Role> createRole(Role role) {
    return roleRepository
      .existsByName(role.getName())
      .flatMap(roleExists -> {
        if (Boolean.TRUE.equals(roleExists)) {
          throw new IllegalStateException("Role already exists");
        } else {
          return roleRepository.save(role);
        }
      });
  }

  @SuppressWarnings("null")
  @Override
  public Mono<Role> getRoleById(UUID roleId) {
    return roleRepository.findById(roleId);
  }

  @SuppressWarnings("null")
  @Override
  @Transactional
  public Mono<Void> deleteRoleById(UUID roleId) {
    return roleRepository.deleteById(roleId);
  }

  @Override
  public Flux<Role> findAllRoles() {
    return roleRepository.findAll();
  }

  @Override
  @Transactional
  public Mono<Void> deleteAllRoles() {
    return roleRepository.deleteAll();
  }

  @Override
  public Mono<Role> getRoleByName(String roleName) {
    return roleRepository.findByName(roleName);
  }
}
