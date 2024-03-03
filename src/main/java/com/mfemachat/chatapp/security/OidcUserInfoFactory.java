package com.mfemachat.chatapp.security;

import java.util.Map;

import com.mfemachat.chatapp.exception.OidcAuthenticationProcessingException;

public class OidcUserInfoFactory {
    private OidcUserInfoFactory() {}

  public static UserInfo getOidcUserInfo(
    String registrationId,
    Map<String, Object> attributes
  ) {
    if (registrationId.equalsIgnoreCase("google")) {
      return new GoogleOidcUserInfo(attributes);
    } else {
      throw new OidcAuthenticationProcessingException(
        "Sorry! Login with " + registrationId + " is not supported yet."
      );
    }
  }
}
