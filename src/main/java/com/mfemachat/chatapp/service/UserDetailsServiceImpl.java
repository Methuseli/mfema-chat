package com.mfemachat.chatapp.service;

import com.mfemachat.chatapp.data.RoleRepository;
import com.mfemachat.chatapp.data.UserRepository;
import com.mfemachat.chatapp.models.Role;
import com.mfemachat.chatapp.util.CustomSQL;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@AllArgsConstructor
public class UserDetailsServiceImpl implements ReactiveUserDetailsService {

  private UserRepository userRepository;
  private CustomSQL customSQL;
  private RoleRepository roleRepository;

  private Collection<? extends GrantedAuthority> getAuthorities(
    Collection<Role> roles
  ) {
    return roles
      .stream()
      .map(role -> new SimpleGrantedAuthority(role.getName()))
      .collect(Collectors.toList());
  }

  @SuppressWarnings("null")
  @Override
  public Mono<UserDetails> findByUsername(String email) {
    return userRepository
      .existsByEmail(email)
      .flatMap(exists -> {
        if (!Boolean.TRUE.equals(exists)) {
          throw new UsernameNotFoundException(
            "User with email " + email + " not found"
          );
        } else {
          return userRepository
            .findByEmail(email)
            .flatMap(user ->
              customSQL
                .getUserRolesByUserId(user.getId())
                .flatMap(userRole ->
                  roleRepository.findById(userRole.getRoleUuid())
                )
                .collectList()
                .map(roleList ->
                  new User(email, user.getPassword(), getAuthorities(roleList))
                )
            ).doOnError(error -> log.error("Error laoding user: {} ", error.getMessage()));
        }
      });
  }
}
