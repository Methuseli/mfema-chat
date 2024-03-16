package com.mfemachat.chatapp.controller;

import com.mfemachat.chatapp.dto.LoginDto;
import com.mfemachat.chatapp.dto.RegisterUserDto;
import com.mfemachat.chatapp.dto.UserDto;
import com.mfemachat.chatapp.security.AuthenticationManager;
import com.mfemachat.chatapp.security.TokenProvider;
import com.mfemachat.chatapp.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {

  private UserService userService;
  private PasswordEncoder passwordEncoder;
  private TokenProvider tokenProvider;
  private AuthenticationManager authenticationManager;

  @PostMapping("/signup")
  public Mono<ResponseEntity<UserDto>> userSignUp(
    @RequestBody RegisterUserDto userDto
  ) {
    // user.setPassword(passwordEncoder.encode(user.getPassword()));
    return userService
      .createUser(
        userDto
      )
      .map(savedUser ->
        ResponseEntity
          .status(HttpStatus.CREATED)
          .body(
            UserDto
              .builder()
              .username(savedUser.getUsername())
              .email(savedUser.getEmail())
              .firstname(savedUser.getFirstname())
              .middlename(savedUser.getMiddlename())
              .lastname(savedUser.getLastname())
              .created(savedUser.getCreated())
              .roles(savedUser.getRoles())
              .build()
          )
      );
  }

  @PostMapping("/login")
  public Mono<ResponseEntity<?>> login(@RequestBody LoginDto loginDto) {
    String email = loginDto.getEmail();
    String password = loginDto.getPassword();

    return userService
      .existsByEmail(email)
      .flatMap(userExists -> {
        if (Boolean.FALSE.equals(userExists)) {
          return Mono.just(
            ResponseEntity
              .status(HttpStatus.UNAUTHORIZED)
              .body("Incorrect email address or password!")
          );
        } else {
          return userService
            .getUserByEmail(email)
            .flatMap(user -> {
              if (user.getAuthProvider() != null) {
                String message =
                  "You signed up using " +
                  user.getAuthProvider() +
                  ", Please login with " +
                  user.getAuthProvider() +
                  "!";
                return Mono.just(
                  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message)
                );
              }
              if (!this.passwordEncoder.matches(password, user.getPassword())) {
                return Mono.just(
                  ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Incorrect email address or password!!")
                );
              }
              String token =
                this.tokenProvider.generateTokenFromUserId(
                    user.getId().toString()
                  );
              return authenticationManager
                .authenticate(
                  new UsernamePasswordAuthenticationToken(token, token)
                )
                .doOnNext(authentication ->
                  SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication)
                )
                .map(authentication -> {
                  @SuppressWarnings("null")
                  ResponseCookie cookie = ResponseCookie
                    .from("token", token)
                    .httpOnly(true)
                    .path("/")
                    .build();
                  return ResponseEntity
                    .ok()
                    .header("Set-Cookie", cookie.toString())
                    .body("Login Successful");
                })
                .onErrorResume(error -> {
                  if (error instanceof AuthenticationException) {
                    return Mono.just(
                      ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Incorrect email address or password!!!")
                    );
                  } else {
                    return Mono.just(
                      ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Some error occurred")
                    );
                  }
                });
            });
        }
      });
  }
}
