package com.mfemachat.chatapp.controller;

import com.mfemachat.chatapp.models.User;
import com.mfemachat.chatapp.security.CurrentUser;
import com.mfemachat.chatapp.security.UserPrincipal;
import com.mfemachat.chatapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class UserController {

  @Autowired
  private UserService userService;

  public Mono<String> index(
    @AuthenticationPrincipal Mono<OAuth2User> oauth2User
  ) {
    return oauth2User
      .map(OAuth2User::getAttributes)
      .map(name -> String.format("Hi, %s", name));
  }

  @GetMapping("/")
  public Mono<ResponseEntity<User>> getCurrentUser(
    @CurrentUser UserPrincipal userPrincipal
  ) {
    return userService
      .getUserById(userPrincipal.getId())
      .map(ResponseEntity::ok)
      .defaultIfEmpty(ResponseEntity.notFound().build());
  }
}
