package com.mfemachat.chatapp.config;

import com.mfemachat.chatapp.data.RoleRepository;
import com.mfemachat.chatapp.data.UserRepository;
import com.mfemachat.chatapp.security.AuthenticationManager;
import com.mfemachat.chatapp.security.HttpCookieOAuth2AuthorizationRequestRepository;
import com.mfemachat.chatapp.security.OAuth2UserService;
import com.mfemachat.chatapp.security.SecurityContextRepository;
import com.mfemachat.chatapp.security.TokenAuthenticationFilter;
import com.mfemachat.chatapp.security.TokenProvider;
import com.mfemachat.chatapp.service.UserService;
import com.mfemachat.chatapp.service.UserServiceImpl;
import com.mfemachat.chatapp.util.CustomSQL;
import com.mfemachat.chatapp.util.UserMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.authentication.ReactiveOidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.web.server.ServerAuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoderFactory;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
// @AllArgsConstructor
public class SecurityConfig {

  @Autowired
  private ReactiveUserDetailsService userDetailsService;

  @Autowired
  private WebConfig webConfig;

  @Autowired
  private ServerAuthenticationSuccessHandler authenticationSuccessHandler;

  @Autowired
  private ServerAuthenticationFailureHandler authenticationFailureHandler;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private CustomSQL customSQL;

  @Bean
  UserMapper userMapper() {
    return Mappers.getMapper(UserMapper.class);
  }

  @Bean
  public UserService userService() {
    return new UserServiceImpl(
      userRepository,
      userMapper(),
      customSQL,
      roleRepository
    );
  }

  // jwt http-only Cookie authorization and authentication
  @Bean
  TokenProvider tokenProvider() {
    return new TokenProvider(webConfig);
  }

  // @Bean
  // ReactiveUserDetailsService userDetailsService() {
  //   return new UserDetailsServiceImpl(userRepository, customSQL, roleRepository);
  // }

  @Bean
  TokenAuthenticationFilter tokenAuthenticationFilter() {
    return new TokenAuthenticationFilter(
      tokenProvider(),
      userDetailsService,
      securityContextRepository()
    );
  }

  @Bean
  AuthenticationManager authenticationManager() {
    return new AuthenticationManager(tokenProvider(), userDetailsService);
  }

  @Bean
  ServerSecurityContextRepository securityContextRepository() {
    return new SecurityContextRepository(authenticationManager());
  }

  @Bean
  @Order(0)
  public SecurityWebFilterChain configure(ServerHttpSecurity http) {
    return http
      .authorizeExchange(authorize ->
        authorize
          .pathMatchers("/login/oauth2/code/**")
          .permitAll()
          .anyExchange()
          .authenticated()
      )
      .oauth2Login(oauth2 ->
        oauth2
          .authorizationRequestRepository(
            serverAuthorizationRequestRepository()
          )
          .authenticationFailureHandler(authenticationFailureHandler)
          .authenticationSuccessHandler(authenticationSuccessHandler)
      )
      .httpBasic(Customizer.withDefaults())
      .securityContextRepository(securityContextRepository())
      .authenticationManager(authenticationManager())
      .addFilterAfter(
        tokenAuthenticationFilter(),
        SecurityWebFiltersOrder.AUTHORIZATION
      )
      .build();
  }

  @Bean
  public PasswordEncoder encoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public ReactiveOAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
    final OAuth2UserService delegate = new OAuth2UserService(
      userRepository,
      roleRepository,
      customSQL,
      encoder()
    );

    return delegate::loadUser;
  }

  @Bean
  public ReactiveJwtDecoderFactory<ClientRegistration> idTokenDecoderFactory() {
    ReactiveOidcIdTokenDecoderFactory idTokenDecoderFactory = new ReactiveOidcIdTokenDecoderFactory();
    idTokenDecoderFactory.setJwsAlgorithmResolver(clientRegistration ->
      SignatureAlgorithm.RS256
    );
    return idTokenDecoderFactory;
  }

  @Bean
  public ServerAuthorizationRequestRepository<OAuth2AuthorizationRequest> serverAuthorizationRequestRepository() {
    return new HttpCookieOAuth2AuthorizationRequestRepository();
  }
}
