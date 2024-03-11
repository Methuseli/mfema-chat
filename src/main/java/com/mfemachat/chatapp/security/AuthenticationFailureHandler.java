package com.mfemachat.chatapp.security;

import com.mfemachat.chatapp.util.CookieUtils;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthenticationFailureHandler
  implements ServerAuthenticationFailureHandler {

  private ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

  private URI location;

  private HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

  // @Autowired
  public AuthenticationFailureHandler(
    HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository
  ) {
    this.authorizationRequestRepository = authorizationRequestRepository;
  }

  // public Mono<Void> sendRedirect(ServerWebExchange exchange, URI location) {
  //   Assert.notNull(exchange, "exchange cannot be null");
  //   Assert.notNull(location, "location cannot be null");
  //   return Mono.fromRunnable(() -> {
  //     ServerHttpResponse response = exchange.getResponse();
  //     response.setStatusCode(this.httpStatus);
  //     URI newLocation = createLocation(exchange, location);
  //     log.debug("Redirecting to '%s'", newLocation);
  //     response.getHeaders().setLocation(newLocation);
  //   });
  // }

  // private URI createLocation(ServerWebExchange exchange, URI location) {
  //   if (!this.contextRelative) {
  //     return location;
  //   }
  //   String url = location.toASCIIString();
  //   if (url.startsWith("/")) {
  //     String context = exchange.getRequest().getPath().contextPath().value();
  //     return URI.create(context + url);
  //   }
  //   return location;
  // }

  @Override
  public Mono<Void> onAuthenticationFailure(
    WebFilterExchange webFilterExchange,
    AuthenticationException exception
  ) {
    log.debug("Authentication Failure");
    return CookieUtils
      .getCookie(
        webFilterExchange.getExchange().getRequest(),
        HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME
      )
      .flatMap(cookie -> {
        String targetUrl = "/error";
        // if (cookie.getValue().isEmpty()) {
        //   targetUrl = cookie.getValue();
        // } else {
        //   targetUrl = "/login";
        // }

        targetUrl =
          UriComponentsBuilder
            .fromUriString(targetUrl)
            .queryParam("error", exception.getLocalizedMessage())
            .build()
            .toUriString();

        this.authorizationRequestRepository.removeAuthorizationRequestCookies(
            webFilterExchange
          );
        try {
          this.location = new URI(targetUrl);

          this.redirectStrategy.sendRedirect(
              webFilterExchange.getExchange(),
              location
            ).subscribe();
        } catch (URISyntaxException ex) {
          log.error("Error creating URI for {}", ex.getMessage());
        }
        return Mono.empty();
      });
  }
}
