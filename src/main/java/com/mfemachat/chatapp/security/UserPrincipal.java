package com.mfemachat.chatapp.security;

import com.mfemachat.chatapp.models.User;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import reactor.core.publisher.Mono;

public class UserPrincipal extends DefaultOidcUser implements UserDetails {

  private UUID id;
  private String email;
  private String password;
  private String username;
  private Collection<? extends GrantedAuthority> authorities;
  private Map<String, Object> attributes;
  private final OidcIdToken idToken;
	private final OidcUserInfo userInfo;

  public UserPrincipal(
    UUID id,
    String email,
    String password,
    String username,
    Collection<? extends GrantedAuthority> authorities,
    OidcIdToken idToken,
    OidcUserInfo userInfo
  ) {
    super(authorities, idToken, userInfo);
    this.id = id;
    this.email = email;
    this.password = password;
    this.username = username;
    this.authorities = authorities;
    this.idToken = idToken;
    this.userInfo = userInfo;
  }

  public static Mono<UserPrincipal> create(Mono<User> userMono, OidcIdToken idToken, OidcUserInfo userInfo) {
    return userMono.flatMap(user ->
      Mono.just(
        new UserPrincipal(
          user.getId(),
          user.getEmail(),
          user.getPassword(),
          user.getUsername(),
          user
            .getRoles()
            .stream()
            .map(role -> new SimpleGrantedAuthority(role.getName()))
            .collect(Collectors.toList()),
            idToken,
            userInfo
        )
      )
    );
  }

  public static Mono<UserPrincipal> create(
    Mono<User> user,
    Map<String, Object> attributes,
    OidcIdToken idToken, 
    OidcUserInfo oidcUserInfo
  ) {
    Mono<UserPrincipal> userPrincipal = UserPrincipal.create(user, idToken, oidcUserInfo);
    userPrincipal.subscribe(userSub -> userSub.setAttributes(attributes));
    return userPrincipal;
  }

  public UUID getId() {
    return id;
  }

  @Override
  public String getEmail() {
    return email;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return this.attributes;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return this.authorities;
  }

  @Override
  public String getName() {
    return String.valueOf(id);
  }

  @Override
  public String getPassword() {
    return this.password;
  }

  @Override
  public String getUsername() {
    return this.username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public void setAttributes(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  @Override
  public Map<String, Object> getClaims() {
    return this.getAttributes();
  }

  @Override
  public OidcUserInfo getUserInfo() {
    return this.userInfo;
  }

  @Override
  public OidcIdToken getIdToken() {
    return this.idToken;
  }
}
