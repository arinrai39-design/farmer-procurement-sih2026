package com.sih.procurement.security;

import com.sih.procurement.entity.Role;

public record AuthenticatedUser(Long userId, Long farmerId, Role role, String username) {}
