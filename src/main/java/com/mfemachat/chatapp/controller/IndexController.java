package com.mfemachat.chatapp.controller;

import com.mfemachat.chatapp.models.User;
import com.mfemachat.chatapp.security.CurrentUser;
import com.mfemachat.chatapp.security.UserPrincipal;
import com.mfemachat.chatapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class IndexController {

  @Autowired
  private UserService userService;

  @GetMapping("/currentUser")
  public Mono<ResponseEntity<User>> getCurrentUser(
    @CurrentUser UserPrincipal userPrincipal
  ) {
    return userService
      .getUserById(userPrincipal.getId())
      .map(ResponseEntity::ok)
      .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @GetMapping("/")
  public Mono<String> home(
    @CurrentUser UserPrincipal userPrincipal
  ) {
    return Mono.just("Welcome");
  }
}
