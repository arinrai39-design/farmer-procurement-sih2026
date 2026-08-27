package com.sih.procurement.security;

import com.sih.procurement.entity.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
  @Test
  void issuedTokenParsesToAuthenticatedUser() {
    JwtService service = new JwtService("unit-test-secret-that-is-long-enough", 60);

    String token = service.issue(7L, 3L, "9876501001", Role.FARMER);
    AuthenticatedUser user = service.parse(token).orElseThrow();

    assertEquals(7L, user.userId());
    assertEquals(3L, user.farmerId());
    assertEquals(Role.FARMER, user.role());
  }

  @Test
  void tamperedTokenIsRejected() {
    JwtService service = new JwtService("unit-test-secret-that-is-long-enough", 60);
    String token = service.issue(7L, 3L, "9876501001", Role.FARMER);

    assertTrue(service.parse(token + "x").isEmpty());
  }
}
