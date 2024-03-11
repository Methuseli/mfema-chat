package com.mfemachat.chatapp.util;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import java.io.IOException;
import java.util.Base64;
import lombok.NoArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.util.SerializationUtils;
import reactor.core.publisher.Mono;

// @Slf4j
@NoArgsConstructor
public class CookieUtils {

  // private static final ObjectMapper objectMapper = new ObjectMapper();

  @SuppressWarnings("null")
  public static Mono<HttpCookie> getCookie(
    ServerHttpRequest request,
    String name
  ) {
    MultiValueMap<String, HttpCookie> cookies = request.getCookies();

    if (!cookies.isEmpty() && cookies.containsKey(name)) {
      // log.info("Cookie {} ", cookies);
      return Mono.just(cookies.getFirst(name));
    }
    return Mono.empty();
  }

  public static void addCookie(
    ServerHttpResponse response,
    String name,
    String value,
    int maxAge
  ) {
    @SuppressWarnings("null")
    ResponseCookie cookie = ResponseCookie
      .from(name, value)
      .httpOnly(true)
      .path("/")
      .maxAge(maxAge)
      .build();
    response.addCookie(cookie);
  }

  public static void deleteCookie(
    ServerHttpRequest request,
    ServerHttpResponse response,
    String name
  ) {
    MultiValueMap<String, HttpCookie> cookies = request.getCookies();
    if (!cookies.isEmpty() && cookies.containsKey(name)) {
      @SuppressWarnings("null")
      ResponseCookie cookie = ResponseCookie
        .from(name, "")
        .path("/")
        .maxAge(0)
        .build();
      response.addCookie(cookie);
    }
  }

  public static String serialize(Object object) {
    return Base64
      .getUrlEncoder()
      .encodeToString(SerializationUtils.serialize(object));
  }

  // public static <T> T deserialize(HttpCookie cookie, Class<T> cls) {
  //   try {
  //     log.info("Deserializing cookie {}", cookie);
  //     return objectMapper.readValue(
  //       Base64.getUrlDecoder().decode(cookie.getValue()),
  //       cls
  //     );
  //   } catch (IOException e) {
  //     log.error(e.getMessage(), e);
  //     return null;
  //   }
  // }
  public static <T> T deserialize(HttpCookie cookie, Class<T> cls) {
    return cls.cast(
      SerializationUtils.deserialize(
        Base64.getUrlDecoder().decode(cookie.getValue())
      )
    );
  }
}
