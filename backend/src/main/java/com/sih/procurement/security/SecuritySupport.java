package com.sih.procurement.security;

import com.sih.procurement.entity.Role;
import com.sih.procurement.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecuritySupport {
  public AuthenticatedUser currentUser() {
    Object principal = SecurityContextHolder.getContext().getAuthentication() == null
        ? null
        : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof AuthenticatedUser user) return user;
    throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required.");
  }

  public boolean hasRole(Role role) {
    return currentUser().role() == role;
  }

  public void requireFarmerOwner(Long farmerId) {
    AuthenticatedUser user = currentUser();
    if (user.role() == Role.ADMIN || user.role() == Role.OFFICER) return;
    if (user.farmerId() == null || !user.farmerId().equals(farmerId)) {
      throw new ApiException(HttpStatus.FORBIDDEN, "You are not allowed to access this farmer record.");
    }
  }

  public void requireStaff() {
    Role role = currentUser().role();
    if (role != Role.ADMIN && role != Role.OFFICER) {
      throw new ApiException(HttpStatus.FORBIDDEN, "Officer access required.");
    }
  }
}
