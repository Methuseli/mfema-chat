package com.mfemachat.chatapp.controller;

import com.mfemachat.chatapp.models.User;
import com.mfemachat.chatapp.security.CurrentUser;
import com.mfemachat.chatapp.security.TokenProvider;
import com.mfemachat.chatapp.security.UserPrincipal;
import com.mfemachat.chatapp.service.UserService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class IndexController {

  @Autowired
  private UserService userService;

  @Autowired
  private TokenProvider tokenProvider;

  @GetMapping("/current-user")
  public Mono<ResponseEntity<User>> getCurrentUser(
    @CurrentUser UserPrincipal userPrincipal,
    ServerWebExchange exchange
  ) {
    if (userPrincipal == null) {
      String token = tokenProvider.getJwtFromCookies(exchange.getRequest());
      String subject = tokenProvider.getJwtTokenSubject(token);
      return userService
        .getUserById(UUID.fromString(subject))
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    return userService
      .getUserById(userPrincipal.getId())
      .map(ResponseEntity::ok)
      .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @GetMapping("/")
  public Mono<String> home() {
    return Mono.just("Welcome");
  }
}
