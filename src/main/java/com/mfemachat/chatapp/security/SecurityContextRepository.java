package com.mfemachat.chatapp.security;

import com.mfemachat.chatapp.util.CookieUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Slf4j
public class SecurityContextRepository
  implements ServerSecurityContextRepository {

  private AuthenticationManager authenticationManager;

  @Override
  public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
    return Mono.empty();
  }

  @Override
  public Mono<SecurityContext> load(ServerWebExchange exchange) {
    return CookieUtils
      .getCookie(exchange.getRequest(), "token")
      .map(HttpCookie::getValue)
      .flatMap(token -> {
        log.info("Token {}", token);
        Authentication auth = new UsernamePasswordAuthenticationToken(
          token,
          token
        );
        log.info("Authentication: {}", auth);
        return this.authenticationManager.authenticate(auth)
          .map(SecurityContextImpl::new);
      });
  }
  // @Override
  // public Mono<SecurityContext> load(ServerWebExchange exchange) {
  //   return Mono.justOrEmpty(CookieUtils.getCookie(exchange.getRequest(), "token"))
  //           .flatMap(token -> {
  //               Authentication auth = new UsernamePasswordAuthenticationToken(token, token);
  //               return this.authenticationManager.authenticate(auth).map(SecurityContextImpl::new);
  //           });
  // }
}
