package com.mfemachat.chatapp.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;

import com.mfemachat.chatapp.util.CookieUtils;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class SecurityContextRepository
  implements ServerSecurityContextRepository {

  private AuthenticationManager authenticationManager;

  @Override
  public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
    return Mono.empty();
  }

  @Override
  public Mono<SecurityContext> load(ServerWebExchange exchange) {
    return Mono.justOrEmpty(CookieUtils.getCookie(exchange.getRequest(), "token"))
            .flatMap(token -> {
                Authentication auth = new UsernamePasswordAuthenticationToken(token, token);
                return this.authenticationManager.authenticate(auth).map(SecurityContextImpl::new);
            });
  }
}
