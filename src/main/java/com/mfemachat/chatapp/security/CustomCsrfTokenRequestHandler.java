package com.mfemachat.chatapp.security;

import org.springframework.http.HttpCookie;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.csrf.XorServerCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class CustomCsrfTokenRequestHandler
  extends ServerCsrfTokenRequestAttributeHandler {

  private final XorServerCsrfTokenRequestAttributeHandler delegate = new XorServerCsrfTokenRequestAttributeHandler();

  @Override
  public void handle(ServerWebExchange exchange, Mono<CsrfToken> csrfToken) {
    this.delegate.handle(exchange, csrfToken);
  }

  @Override
  public Mono<String> resolveCsrfTokenValue(
    ServerWebExchange exchange,
    CsrfToken csrfToken
  ) {
    HttpCookie cookie = exchange
      .getRequest()
      .getCookies()
      .getFirst("XSRF-TOKEN");

    log.debug("Csrf Token {}", cookie);

    if (cookie != null && StringUtils.hasText(cookie.getValue())) {
        String actualToken = cookie.getValue();
        return Mono.just(actualToken);
    }

    return this.delegate.resolveCsrfTokenValue(exchange, csrfToken);
  }
}
