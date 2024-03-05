package com.mfemachat.chatapp.config;

import com.mfemachat.chatapp.data.RoleRepository;
import com.mfemachat.chatapp.data.UserRepository;
import com.mfemachat.chatapp.security.OAuth2UserService;
import com.mfemachat.chatapp.util.CustomSQL;
import com.mfemachat.chatapp.util.UserMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.authentication.ReactiveOidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoderFactory;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

  private UserRepository userRepository;
  private RoleRepository roleRepository;
  private CustomSQL customSQL;

  public SecurityConfig(
    UserRepository userRepository,
    RoleRepository roleRepository,
    CustomSQL customSQL
  ) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.customSQL = customSQL;
  }

  @Bean
  UserMapper userMapper() {
    return Mappers.getMapper(UserMapper.class);
  }

  @Bean
  public SecurityWebFilterChain configure(ServerHttpSecurity http) {
    return http
      .authorizeExchange(authorize ->
        authorize
          .pathMatchers("/login/oauth2/code/**")
          .permitAll()
          .anyExchange()
          .authenticated()
      )
      .oauth2Login(Customizer.withDefaults())
      .build();
  }

  @Bean
  public ReactiveOAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
    final OAuth2UserService delegate = new OAuth2UserService(
      userRepository,
      roleRepository,
      customSQL
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
}
