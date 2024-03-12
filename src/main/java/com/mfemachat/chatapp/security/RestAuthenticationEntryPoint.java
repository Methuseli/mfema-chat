package com.mfemachat.chatapp.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class RestAuthenticationEntryPoint implements ServerAuthenticationEntryPoint{

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        log.error("Responding with unauthorized error. Message - {}", ex.getMessage());
        return exchange.getResponse().setComplete()
                .doOnSubscribe(subscription -> exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED))
                .doOnTerminate(() -> log.debug("Unauthorized access"));
    }
    
}
