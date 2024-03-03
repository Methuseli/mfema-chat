package com.mfemachat.chatapp.security;

import java.util.Map;

public class GoogleOidcUserInfo extends UserInfo {

  public GoogleOidcUserInfo(Map<String, Object> attributes) {
    super(attributes);
  }

  @Override
  public String getId() {
    return (String) attributes.get("sub");
  }

  @Override
  public String getName() {
    return (String) attributes.get("name");
  }

  @Override
  public String getEmail() {
    return (String) attributes.get("email");
  }
}
