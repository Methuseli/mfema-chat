package com.mfemachat.chatapp.security;

import com.mfemachat.chatapp.config.WebConfig;
import com.mfemachat.chatapp.exception.BadRequestException;
import com.mfemachat.chatapp.util.CookieUtils;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthenticationSuccessHandler
  implements ServerAuthenticationSuccessHandler {

  private TokenProvider tokenProvider;
  private WebConfig appConfig;
  private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
  private ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

  private URI location;

  //   @Autowired
  public AuthenticationSuccessHandler(
    TokenProvider tokenProvider,
    WebConfig appConfig,
    HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository
  ) {
    this.tokenProvider = tokenProvider;
    this.appConfig = appConfig;
    this.httpCookieOAuth2AuthorizationRequestRepository =
      httpCookieOAuth2AuthorizationRequestRepository;
  }

  @Override
  public Mono<Void> onAuthenticationSuccess(
    WebFilterExchange webFilterExchange,
    Authentication authentication
  ) {
    log.info("Successful authentication");
    return determineTargetUrl(webFilterExchange, authentication)
      .flatMap(url -> {
        if (webFilterExchange.getExchange().getResponse().isCommitted()) {
          log.info(
            "Response has already been committed. Unable to redirect to " + url
          );
          return Mono.empty();
        }

        clearAuthenticationAttributes(webFilterExchange);

        try {
          this.location = new URI(url);
          return this.redirectStrategy.sendRedirect(
              webFilterExchange.getExchange(),
              location
            );
        } catch (URISyntaxException ex) {
          log.error("Error creating URI for " + url, ex);
        }
        return Mono.empty();
      });
  }

  @SuppressWarnings("null")
  protected Mono<String> determineTargetUrl(
    WebFilterExchange webFilterExchange,
    Authentication authentication
  ) {
    Mono<String> redirectUri = CookieUtils
      .getCookie(
        webFilterExchange.getExchange().getRequest(),
        HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME
      )
      .map(HttpCookie::getValue)
      .defaultIfEmpty("http://localhost:8080/current-user");

    return redirectUri.flatMap(uri -> {
      log.info("Redirect URI: {}", uri);
      if (!isAuthorizedRedirectUri(uri)) {
        throw new BadRequestException(
          "Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication"
        );
      }

      String token = tokenProvider.createToken(authentication);
      log.info("Token authentication {}", token);

      ResponseCookie cookie = ResponseCookie
        .from("token", token)
        .httpOnly(true)
        .path("/")
        .build();
      webFilterExchange.getExchange().getResponse().addCookie(cookie);

      return Mono.just(
        UriComponentsBuilder
          .fromUriString(uri)
          .queryParam("token", token)
          .build()
          .toUriString()
      );
    });
  }

  protected Mono<Void> clearAuthenticationAttributes(
    WebFilterExchange webFilterExchange
  ) {
    webFilterExchange
      .getExchange()
      .getSession()
      .flatMap(session -> {
        session.getAttributes().remove(WebAttributes.AUTHENTICATION_EXCEPTION);
        return session.save();
      });
    httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(
      webFilterExchange
    );

    return Mono.empty();
  }

  private boolean isAuthorizedRedirectUri(String uri) {
    URI clientRedirectUri = URI.create(uri);

    return appConfig
      .getAuthorizedRedirectUris()
      .stream()
      .anyMatch(authorizedRedirectUri -> {
        // Only validate host and port. Let the clients use different paths if they want to
        URI authorizedURI = URI.create(authorizedRedirectUri);
        return (
          authorizedURI
            .getHost()
            .equalsIgnoreCase(clientRedirectUri.getHost()) &&
          authorizedURI.getPort() == clientRedirectUri.getPort()
        );
      });
  }
}
