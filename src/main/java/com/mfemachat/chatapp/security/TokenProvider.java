package com.mfemachat.chatapp.security;

import com.mfemachat.chatapp.config.WebConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
// import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
// import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TokenProvider {

  private WebConfig appConfig;

  // private static final String ALGORITHM = "EdDSA";

  // @Autowired
  public TokenProvider(WebConfig appConfig) {
    this.appConfig = appConfig;
  }

  // Check out the following if the uncommented method fails "SecretKey key = Keys.hmacShaKeyFor(encodedKeyBytes);",
  // "SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretString));"
  //"SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretString));"
  // public SecretKey generateSecretKeyFromString(
  //   String keyString,
  //   String algorithm
  // ) {
  //   byte[] keyBytes = keyString.getBytes(StandardCharsets.UTF_8);
  //   return new SecretKeySpec(keyBytes, algorithm);
  // }

  public String createToken(Authentication authentication) {
    // log.debug("Creating token");
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Date now = new Date();
    Date expiryDate = new Date(
      now.getTime() + appConfig.getTokenExpirationMsec()
    );

    SecretKey key = Keys.hmacShaKeyFor(
      Decoders.BASE64URL.decode(appConfig.getTokenSecret())
    );

    return Jwts
      .builder()
      .subject(userPrincipal.getId().toString())
      .issuedAt(new Date())
      .signWith(key)
      .expiration(expiryDate)
      .compact();
  }

  public String getJwtFromCookies(ServerHttpRequest request) {
    @SuppressWarnings("null")
    HttpCookie cookie = request
      .getCookies()
      .getFirst(appConfig.getCookieName());
    if (cookie != null) {
      return cookie.getValue();
    } else {
      return null;
    }
  }

  public String getJwtTokenSubject(String token) {
    SecretKey key = Keys.hmacShaKeyFor(
      Decoders.BASE64URL.decode(appConfig.getTokenSecret())
    );
    // Claims claims = Jwts.parser().decryptWith(key).;

    // log.info("Token: {}", token);

    Claims claims = Jwts
      .parser()
      .verifyWith(key)
      .build()
      .parseSignedClaims(token)
      .getPayload();

    return claims.getSubject();
  }

  public String generateTokenFromUsername(String username) {
    SecretKey key = Keys.hmacShaKeyFor(
      Decoders.BASE64URL.decode(appConfig.getTokenSecret())
    );
    return Jwts
      .builder()
      .subject(username)
      .issuedAt(new Date())
      .expiration(
        new Date((new Date()).getTime() + appConfig.getTokenExpirationMsec())
      )
      .signWith(key)
      .compact();
  }

  public boolean validateToken(String authToken) {
    try {
      SecretKey key = Keys.hmacShaKeyFor(
        Decoders.BASE64URL.decode(appConfig.getTokenSecret())
      );
      //   Jwts
      //     .parser()
      //     .setSigningKey(appConfig.getTokenSecret())
      //     .parseClaimsJws(authToken);

      Jwts.parser().verifyWith(key).build().parseSignedClaims(authToken);
      return true;
    } catch (SignatureException ex) {
      log.error("Invalid JWT signature");
    } catch (MalformedJwtException ex) {
      log.error("Invalid JWT token");
    } catch (ExpiredJwtException ex) {
      log.error("Expired JWT token");
    } catch (UnsupportedJwtException ex) {
      log.error("Unsupported JWT token");
    } catch (IllegalArgumentException ex) {
      log.error("JWT claims string is empty.");
    }
    return false;
  }
}
