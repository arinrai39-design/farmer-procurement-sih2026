package com.sih.procurement.service;

import com.sih.procurement.dto.AuthDtos.*;
import com.sih.procurement.entity.*;
import com.sih.procurement.exception.ApiException;
import com.sih.procurement.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserRepository users;
  private final FarmerRepository farmers;
  private final PasswordEncoder encoder;

  public AuthService(UserRepository users, FarmerRepository farmers, PasswordEncoder encoder) {
    this.users = users;
    this.farmers = farmers;
    this.encoder = encoder;
  }

  public AuthResponse register(RegisterRequest req) {
    if (farmers.findByMobile(req.mobile()).isPresent() || farmers.findByFarmerCode(req.farmerId()).isPresent()) {
      throw new ApiException("A farmer with this mobile number or Farmer ID already exists.");
    }
    User user = new User();
    user.username = req.mobile();
    user.displayName = req.fullName();
    user.role = Role.FARMER;
    user.passwordHash = encoder.encode(req.password());
    users.save(user);

    Farmer farmer = new Farmer();
    farmer.user = user;
    farmer.farmerCode = req.farmerId();
    farmer.mobile = req.mobile();
    farmer.address = req.address();
    farmer.village = req.village();
    farmer.district = req.district();
    farmer.state = req.state();
    farmers.save(farmer);
    return new AuthResponse(user.id, farmer.id, user.displayName, user.role, "demo-token-" + user.id);
  }

  public AuthResponse login(LoginRequest req) {
    User user = users.findByUsername(req.identifier())
        .or(() -> farmers.findByFarmerCode(req.identifier()).map(f -> f.user))
        .orElseThrow(() -> new ApiException("Invalid login details."));
    if (!encoder.matches(req.password(), user.passwordHash)) {
      throw new ApiException("Invalid login details.");
    }
    Long farmerId = user.role == Role.FARMER ? farmers.findByUserId(user.id).map(f -> f.id).orElse(null) : null;
    return new AuthResponse(user.id, farmerId, user.displayName, user.role, "demo-token-" + user.id);
  }
}
