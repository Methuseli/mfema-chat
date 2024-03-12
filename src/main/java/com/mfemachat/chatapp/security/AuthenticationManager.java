package com.mfemachat.chatapp.security;

import com.mfemachat.chatapp.exception.JwtAuthenticationException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager {

  private TokenProvider tokenProvider;
  private ReactiveUserDetailsService userDetailsService;

  @Override
  public Mono<Authentication> authenticate(Authentication authentication) {
    String authToken = authentication.getCredentials().toString();
    String username = this.tokenProvider.getJwtTokenSubject(authToken);
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
