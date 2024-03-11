package com.mfemachat.chatapp.security;

import com.mfemachat.chatapp.util.CookieUtils;
// import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.core.log.LogMessage;
// import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

// @AllArgsConstructor
@Slf4j
public class SecurityContextRepository
  implements ServerSecurityContextRepository {

  private AuthenticationManager authenticationManager;

  public static final String DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME =
    "SPRING_SECURITY_CONTEXT";

  private String springSecurityContextAttrName =
    DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME;

  private boolean cacheSecurityContext;

  public SecurityContextRepository(
    AuthenticationManager authenticationManager
  ) {
    this.authenticationManager = authenticationManager;
  }

  public void setSpringSecurityContextAttrName(
    String springSecurityContextAttrName
  ) {
    this.springSecurityContextAttrName = springSecurityContextAttrName;
  }

  public void setCacheSecurityContext(boolean cacheSecurityContext) {
    this.cacheSecurityContext = cacheSecurityContext;
  }

  @Override
  public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
    return exchange
      .getSession()
      .doOnNext(session -> {
        if (context == null) {
          session.getAttributes().remove(this.springSecurityContextAttrName);
          log.debug(
            "Removed SecurityContext stored in WebSession: {}",
            session
          );
        } else {
          session
            .getAttributes()
            .put(this.springSecurityContextAttrName, context);
          log.debug(
            "Saved SecurityContext {} in WebSession: {}",
            context,
            session
          );
        }
      })
      .flatMap(WebSession::changeSessionId);
  }

  @Override
  public Mono<SecurityContext> load(ServerWebExchange exchange) {
    Mono<SecurityContext> result = exchange
      .getSession()
      .flatMap(session -> {
        @SuppressWarnings("null")
        SecurityContext context = (SecurityContext) session.getAttribute(
          this.springSecurityContextAttrName
        );
        if (context != null) {
          log.debug(
            "Found SecurityContext {} in WebSession: {}",
            context,
            session
          );
        } else {
          log.debug("No SecurityContext found in WebSession: '%s'", session);
        }
        return Mono.justOrEmpty(context);
      });

    if (this.cacheSecurityContext) {
      return result.cache();
    } else {
      return result.flatMap(context ->
        context != null
          ? Mono.justOrEmpty(context)
          : CookieUtils
            .getCookie(exchange.getRequest(), "token")
            .map(HttpCookie::getValue)
            .flatMap(token -> {
              // log.info("Token {}", token);
              Authentication auth = new UsernamePasswordAuthenticationToken(
                token,
                token
              );
              // log.info("Authentication: {}", auth);
              return this.authenticationManager.authenticate(auth)
                .map(SecurityContextImpl::new);
            })
      );
    }
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
