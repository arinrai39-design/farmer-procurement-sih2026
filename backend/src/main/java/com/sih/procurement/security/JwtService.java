package com.sih.procurement.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sih.procurement.entity.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
public class JwtService {
  private final ObjectMapper mapper = new ObjectMapper();
  private final byte[] secret;
  private final long expirationMinutes;

  public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
    this.secret = secret.getBytes(StandardCharsets.UTF_8);
    this.expirationMinutes = expirationMinutes;
  }

  public String issue(Long userId, Long farmerId, String username, Role role) {
    long now = Instant.now().getEpochSecond();
    Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
    Map<String, Object> claims = new LinkedHashMap<>();
    claims.put("sub", String.valueOf(userId));
    claims.put("username", username);
    claims.put("role", role.name());
    claims.put("farmerId", farmerId);
    claims.put("iat", now);
    claims.put("exp", now + expirationMinutes * 60);
    String unsigned = encode(header) + "." + encode(claims);
    return unsigned + "." + sign(unsigned);
  }

  public Optional<AuthenticatedUser> parse(String token) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length != 3 || !sign(parts[0] + "." + parts[1]).equals(parts[2])) return Optional.empty();
      Map<?, ?> claims = mapper.readValue(Base64.getUrlDecoder().decode(parts[1]), Map.class);
      Number exp = (Number) claims.get("exp");
      if (exp == null || exp.longValue() < Instant.now().getEpochSecond()) return Optional.empty();
      Long userId = Long.valueOf(String.valueOf(claims.get("sub")));
      Object farmerClaim = claims.get("farmerId");
      Long farmerId = farmerClaim == null || "null".equals(String.valueOf(farmerClaim)) ? null : Long.valueOf(String.valueOf(farmerClaim));
      Role role = Role.valueOf(String.valueOf(claims.get("role")));
      String username = String.valueOf(claims.get("username"));
      return Optional.of(new AuthenticatedUser(userId, farmerId, role, username));
    } catch (Exception ex) {
      return Optional.empty();
    }
  }

  private String encode(Object value) {
    try {
      return Base64.getUrlEncoder().withoutPadding().encodeToString(mapper.writeValueAsBytes(value));
    } catch (Exception ex) {
      throw new IllegalStateException("JWT encoding failed", ex);
    }
  }

  private String sign(String value) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret, "HmacSHA256"));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception ex) {
      throw new IllegalStateException("JWT signing failed", ex);
    }
  }
}
