package com.mfemachat.chatapp.exception;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

public class OidcAuthenticationProcessingException
  extends OAuth2AuthenticationException {

  public OidcAuthenticationProcessingException(String msg) {
    super(msg);
  }
}
