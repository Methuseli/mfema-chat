package com.mfemachat.chatapp.security;

import com.mfemachat.chatapp.util.CookieUtils;
import com.nimbusds.oauth2.sdk.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.server.ServerAuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
  implements ServerAuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME =
    "oauth2_auth_request";
  public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
  private static final int COOKIE_EXPIRE_SECONDS = 180;

  @Override
  public Mono<OAuth2AuthorizationRequest> loadAuthorizationRequest(
    ServerWebExchange exchange
  ) {
    log.debug("Load authorization request");
    return CookieUtils
      .getCookie(
        exchange.getRequest(),
        OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME
      )
      .flatMap(cookie ->
        Mono.just(
          CookieUtils.deserialize(
            (ResponseCookie) cookie,
            OAuth2AuthorizationRequest.class
          )
        )
      );
  }

  @Override
  public Mono<Void> saveAuthorizationRequest(
    OAuth2AuthorizationRequest authorizationRequest,
    ServerWebExchange exchange
  ) {
    log.debug("Save authorization request");
    if (authorizationRequest == null) {
      CookieUtils.deleteCookie(
        exchange.getRequest(),
        exchange.getResponse(),
        OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME
      );
      CookieUtils.deleteCookie(
        exchange.getRequest(),
        exchange.getResponse(),
        REDIRECT_URI_PARAM_COOKIE_NAME
      );
      return Mono.empty();
    }

    CookieUtils.addCookie(
      exchange.getResponse(),
      OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
      CookieUtils.serialize(authorizationRequest),
      COOKIE_EXPIRE_SECONDS
    );
    String redirectUriAfterLogin = exchange
      .getRequest()
      .getQueryParams()
      .getFirst(REDIRECT_URI_PARAM_COOKIE_NAME);
    if (StringUtils.isNotBlank(redirectUriAfterLogin)) {
      CookieUtils.addCookie(
        exchange.getResponse(),
        REDIRECT_URI_PARAM_COOKIE_NAME,
        redirectUriAfterLogin,
        COOKIE_EXPIRE_SECONDS
      );
    }
    return Mono.empty();
  }

  public void removeAuthorizationRequestCookies(WebFilterExchange webFilterExchange) {
    log.debug("removeAuthorizationRequestCookies " + webFilterExchange.getExchange().getRequest());
    CookieUtils.deleteCookie(webFilterExchange.getExchange().getRequest(), webFilterExchange.getExchange().getResponse(), OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
    CookieUtils.deleteCookie(webFilterExchange.getExchange().getRequest(), webFilterExchange.getExchange().getResponse(), REDIRECT_URI_PARAM_COOKIE_NAME);
}

  @Override
  public Mono<OAuth2AuthorizationRequest> removeAuthorizationRequest(
    ServerWebExchange exchange
  ) {
    log.debug("Remove authorization request");
    return this.loadAuthorizationRequest(exchange);
  }
}
