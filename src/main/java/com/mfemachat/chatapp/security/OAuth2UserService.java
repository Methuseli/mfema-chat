package com.mfemachat.chatapp.security;

import com.mfemachat.chatapp.data.RoleRepository;
import com.mfemachat.chatapp.data.UserRepository;
import com.mfemachat.chatapp.exception.OidcAuthenticationProcessingException;
import com.mfemachat.chatapp.models.Role;
import com.mfemachat.chatapp.models.User;
import com.mfemachat.chatapp.util.CustomSQL;

import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.converter.ClaimTypeConverter;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Mono;

@Slf4j
public class OAuth2UserService
  implements ReactiveOAuth2UserService<OidcUserRequest, OidcUser> {

  private UserRepository userRepository;
  private RoleRepository roleRepository;
  private CustomSQL customSQL;
  private ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> defaultOauth2UserService = new DefaultReactiveOAuth2UserService();
  private static final Converter<Map<String, Object>, Map<String, Object>> DEFAULT_CLAIM_TYPE_CONVERTER = new ClaimTypeConverter(
    OidcReactiveOAuth2UserService.createDefaultClaimTypeConverters()
  );

  private Function<ClientRegistration, Converter<Map<String, Object>, Map<String, Object>>> claimTypeConverterFactory = clientRegistration ->
    DEFAULT_CLAIM_TYPE_CONVERTER;

  private static final String INVALID_USER_INFO_RESPONSE_ERROR_CODE =
    "invalid_user_info_response";

  // @Autowired
  public OAuth2UserService(
    UserRepository userRepository,
    RoleRepository roleRepository,
    CustomSQL customSQL
  ) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.customSQL = customSQL;
  }

  @Override
  public Mono<OidcUser> loadUser(OidcUserRequest userRequest)
    throws OAuth2AuthenticationException {
    final OidcReactiveOAuth2UserService delegate = new OidcReactiveOAuth2UserService();
    log.debug("Overriding oauth2UserService");

    // return (userRequest) -> {
    // Delegate to the default implementation for loading a user
    return delegate
      .loadUser(userRequest)
      .flatMap(oidcUser -> 
        processOAuth2User(userRequest, oidcUser))
      .doOnError(error -> log.error("Error processing OAuth2 user: {}", error.getMessage()));
    // };
  }

  private Mono<OidcUser> processOAuth2User(
    OidcUserRequest oidcUserRequest,
    OidcUser oidcUser
  ) {
    Mono<OidcUserInfo> oidcUserInfoMono = getUserInfo(oidcUserRequest);

    UserInfo userInfo = OidcUserInfoFactory.getOidcUserInfo(
      oidcUserRequest.getClientRegistration().getRegistrationId(),
      oidcUser.getAttributes()
    );

    if (ObjectUtils.isEmpty(userInfo.getEmail())) {
      return Mono.error(
        new OidcAuthenticationProcessingException(
          "Email not found from OAuth2 provider"
        )
      );
    }

    return userRepository
      .existsByEmail(userInfo.getEmail())
      .flatMap(exists -> {
        if (Boolean.TRUE.equals(exists)) {
          return userRepository
            .findByEmail(userInfo.getEmail())
            .flatMap(user -> {
              if (user.getAuthProvider() != null) {
                if (
                  !user
                    .getAuthProvider()
                    .equalsIgnoreCase(
                      oidcUserRequest
                        .getClientRegistration()
                        .getRegistrationId()
                    )
                ) {
                  throw new OidcAuthenticationProcessingException(
                    "Looks like you're signed up with " +
                    user.getAuthProvider() +
                    " account. Please use your " +
                    user.getAuthProvider() +
                    " account to login."
                  );
                }
                return Mono.empty();
              } else {
                throw new OidcAuthenticationProcessingException(
                  "Looks like you signed up directly to the system Please use your email or username to login."
                );
              }
            });
        } else {
          return oidcUserInfoMono.flatMap(oidcUserInfo -> 
            UserPrincipal.create(
              registerNewUser(oidcUserRequest, userInfo),
              userInfo.getAttributes(),
              oidcUserRequest.getIdToken(),
              oidcUserInfo
            )
          );
        }
      });
  }

  private Mono<User> registerNewUser(
    OidcUserRequest oidcUserRequest,
    UserInfo userInfo
  ) {
    User user = new User();
    String[] splitName = userInfo.getName().split(" ");
    String password = RandomStringUtils.randomAlphanumeric(16);
    String username = userInfo.getName().replaceAll("\\s", "");

    return getUsername(username)
      .flatMap(availableUsername -> {
        user.setAuthProvider(
          oidcUserRequest.getClientRegistration().getRegistrationId()
        );
        user.setUsername(availableUsername);
        user.setEmail(userInfo.getEmail());
        user.setFirstname(splitName[0]);
        user.setLastname(splitName[splitName.length - 1]);
        user.setPassword(password);
        user.setMiddlename("");
        return roleRepository
          .findByName("USER")
          .flatMap(role -> {
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            user.setRoles(roles);
            return userRepository
              .save(user)
              .flatMap(newUser -> {
                customSQL.saveUserRoles(newUser.getId(), role.getId());
                return Mono.just(newUser);
              });
          });
      });
  }

  private Mono<String> getUsername(String username) {
    return Mono.defer(() -> {
      final String newUsername =
        username + RandomStringUtils.randomAlphanumeric(1);
      return userRepository
        .existsByUsername(newUsername)
        .flatMap(userExists ->
          Boolean.TRUE.equals(userExists)
            ? getUsername(newUsername)
            : Mono.just(newUsername)
        );
    });
  }

  private Mono<OidcUserInfo> getUserInfo(OidcUserRequest userRequest) {
    return this.defaultOauth2UserService.loadUser(userRequest)
      .map(OAuth2User::getAttributes)
      .map(claims -> convertClaims(claims, userRequest.getClientRegistration()))
      .map(OidcUserInfo::new)
      .doOnNext(userInfo -> {
        String subject = userInfo.getSubject();
        if (
          subject == null ||
          !subject.equals(userRequest.getIdToken().getSubject())
        ) {
          OAuth2Error oauth2Error = new OAuth2Error(
            INVALID_USER_INFO_RESPONSE_ERROR_CODE
          );
          throw new OAuth2AuthenticationException(
            oauth2Error,
            oauth2Error.toString()
          );
        }
      });
  }

  @SuppressWarnings("null")
  private Map<String, Object> convertClaims(
    Map<String, Object> claims,
    ClientRegistration clientRegistration
  ) {
    Converter<Map<String, Object>, Map<String, Object>> claimTypeConverter =
      this.claimTypeConverterFactory.apply(clientRegistration);
    return (claimTypeConverter != null)
      ? claimTypeConverter.convert(claims)
      : DEFAULT_CLAIM_TYPE_CONVERTER.convert(claims);
  }
}
