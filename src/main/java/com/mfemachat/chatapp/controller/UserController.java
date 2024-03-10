package com.mfemachat.chatapp.controller;

import com.mfemachat.chatapp.dto.LoginDto;
import com.mfemachat.chatapp.dto.RegisterUserDto;
import com.mfemachat.chatapp.models.User;
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
  public Mono<ResponseEntity<String>> userSignUp(
    @RequestBody RegisterUserDto userDto
  ) {
    // user.setPassword(passwordEncoder.encode(user.getPassword()));
    return userService
      .createUser(
        User
          .builder()
          .username(userDto.getUsername())
          .password(passwordEncoder.encode(userDto.getPassword()))
          .email(userDto.getEmail())
          .firstname(userDto.getFirstname())
          .middlename(userDto.getMiddlename())
          .lastname(userDto.getLastname())
          .build(),
        userDto.getRoles()
      )
      .map(savedUser ->
        ResponseEntity.status(HttpStatus.CREATED).body("Successfully registerd")
      )
      .onErrorResume(error -> {
        if (error instanceof IllegalStateException) {
          return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body("User with email already exists"));
        } else if (error instanceof IllegalArgumentException) {
          return Mono.just(
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid roles")
          );
        } else {
          return Mono.just(
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong")
          );
        }
      });
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
              .body("Incorrect email address or password")
          );
        } else {
          try {
            return authenticationManager
              .authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
              )
              .doOnNext(authentication ->
                SecurityContextHolder
                  .getContext()
                  .setAuthentication(authentication)
              )
              .map(authentication -> {
                String jwt = tokenProvider.generateTokenFromUsername(email);
                @SuppressWarnings("null")
                ResponseCookie cookie = ResponseCookie
                  .from("jwt", jwt)
                  .httpOnly(true)
                  .path("/")
                  .build();
                return ResponseEntity
                  .ok()
                  .header("Set-Cookie", cookie.toString())
                  .body("Login Successful");
              });
          } catch (AuthenticationException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect email address or password"));
          }
        }
      });
  }
}
