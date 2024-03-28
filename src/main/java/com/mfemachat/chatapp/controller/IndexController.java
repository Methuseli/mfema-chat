package com.mfemachat.chatapp.controller;

import com.mfemachat.chatapp.models.User;
import com.mfemachat.chatapp.security.CurrentUser;
import com.mfemachat.chatapp.security.TokenProvider;
import com.mfemachat.chatapp.security.UserPrincipal;
import com.mfemachat.chatapp.service.UserService;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
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

  @SuppressWarnings("null")
  @GetMapping("/csrf")
  public Mono<ResponseEntity<String>> csrfToken(ServerWebExchange exchange) {
    Mono<CsrfToken> csrfToken = exchange.getAttribute(CsrfToken.class.getName());
    log.debug("Exchange attributes {}",exchange.getRequest().getCookies());
    return csrfToken.map(token -> {
      if(token == null) {
        return ResponseEntity.badRequest().build();
      } else {
        log.debug("Token {}", token.getToken());
        return ResponseEntity.ok().body("");
      }
    });
  }

  // @GetMapping("/error")
  // public Mono<ResponseEntity<?>> error() {

  // }
}
