package com.sih.procurement.dto;

import com.sih.procurement.entity.Role;
import jakarta.validation.constraints.*;

public class AuthDtos {
  public record RegisterRequest(
      @NotBlank String fullName,
      @NotBlank String mobile,
      @NotBlank String farmerId,
      @NotBlank String address,
      @NotBlank String village,
      @NotBlank String district,
      @NotBlank String state,
      @Size(min = 6) String password) {}

  public record LoginRequest(@NotBlank String identifier, @NotBlank String password) {}

  public record AuthResponse(Long userId, Long farmerId, String name, Role role, String token) {}
}
