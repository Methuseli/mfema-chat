package com.mfemachat.chatapp.service;

import com.mfemachat.chatapp.data.RoleRepository;
import com.mfemachat.chatapp.models.Role;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RoleServiceImpl implements RoleService {

  private RoleRepository roleRepository;

  public RoleServiceImpl(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  @Override
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
  public Mono<Void> deleteRoleById(UUID roleId) {
    return roleRepository.deleteById(roleId);
  }

  @Override
  public Flux<Role> findAllRoles() {
    return roleRepository.findAll();
  }

  @Override
  public Mono<Void> deleteAllRoles() {
    return roleRepository.deleteAll();
  }

  @Override
  public Mono<Role> getRoleByName(String roleName) {
    return roleRepository.findByName(roleName);
  }
}
