package com.mfemachat.chatapp.security;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
public class TokenAuthenticationFilter implements WebFilter {

  private TokenProvider tokenProvider;
  private ReactiveUserDetailsService userDetailsService;
  private ServerSecurityContextRepository securityContextRepository;

  @SuppressWarnings("null")
  @Override
  public Mono<Void> filter(
    @SuppressWarnings("null") ServerWebExchange exchange,
    @SuppressWarnings("null") WebFilterChain chain
  ) {
    log.debug("Token Filter executed");
    ServerHttpRequest request = exchange.getRequest();
    // ServerHttpResponse response = exchange.getResponse();

    String jwt = getJwtFromRequest(request);
    String jwtCookie = parseJwt(request);
    String username = "";

    if (
      StringUtils.hasText(jwtCookie) && tokenProvider.validateToken(jwtCookie)
    ) {
      username = tokenProvider.getUsernameFromToken(jwtCookie);
      log.debug("Username JWT COOKIE" + username);
    } else if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
      username = tokenProvider.getUsernameFromToken(jwt);
      log.debug("Username JWT " + username);
    }

    log.debug("OutsideUsername " + username);
    return userDetailsService
      .findByUsername(username)
      .flatMap(userDetails -> {
        log.debug("Userdetails " + userDetails);
        if (userDetails != null) {
          UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
          );

          log.debug("Exchange ", exchange);

          return ReactiveSecurityContextHolder
            .getContext()
            .flatMap(c -> {
              c.setAuthentication(authentication);
              return securityContextRepository
                .save(exchange, c)
                .then(chain.filter(exchange));
            });
        }

        return chain.filter(exchange);
      })
      .doOnSuccess(it -> log.debug("Token filter successful"))
      .doOnError(
        Exception.class,
        ex -> {
          ex.printStackTrace();
          log.error("  Exception throw", ex);
        }
      );
  }

  private String getJwtFromRequest(ServerHttpRequest request) {
    List<String> authorizationList = request.getHeaders().get("Authorization");
    if (authorizationList != null && !authorizationList.isEmpty()) {
      String bearerToken = authorizationList.get(0);

      if (
        StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")
      ) {
        return bearerToken.substring(7, bearerToken.length());
      }
    }
    return null;
  }

  private String parseJwt(ServerHttpRequest request) {
    return tokenProvider.getJwtFromCookies(request);
  }
}
