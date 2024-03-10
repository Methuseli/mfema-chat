package com.mfemachat.chatapp.security;

import com.mfemachat.chatapp.exception.JwtAuthenticationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
@Slf4j
public class AuthenticationManager implements ReactiveAuthenticationManager {

  private TokenProvider tokenProvider;
  private ReactiveUserDetailsService userDetailsService;

  @Override
  public Mono<Authentication> authenticate(Authentication authentication) {
    String authToken = authentication.getCredentials().toString();
    log.info("Authentication token: {}", authToken);
    String username = this.tokenProvider.getUsernameFromToken(authToken);
    return Mono
      .just(this.tokenProvider.validateToken(authToken))
      .filter(valid -> valid)
      .switchIfEmpty(Mono.empty())
      .flatMap(valid -> {
        if (Boolean.TRUE.equals(valid)) {
          return userDetailsService
            .findByUsername(username)
            .map(userDetail ->
              new UsernamePasswordAuthenticationToken(
                userDetail.getUsername(),
                null,
                userDetail.getAuthorities()
              )
            );
        } else {
          throw new JwtAuthenticationException("Invalid token");
        }
      });
  }
}
