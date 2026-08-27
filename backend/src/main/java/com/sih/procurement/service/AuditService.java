package com.sih.procurement.service;

import com.sih.procurement.dto.AuditDtos.AuditLogView;
import com.sih.procurement.entity.AuditLog;
import com.sih.procurement.entity.User;
import com.sih.procurement.repository.AuditLogRepository;
import com.sih.procurement.repository.UserRepository;
import com.sih.procurement.security.SecuritySupport;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {
  private final AuditLogRepository auditLogs;
  private final UserRepository users;
  private final SecuritySupport security;

  public AuditService(AuditLogRepository auditLogs, UserRepository users, SecuritySupport security) {
    this.auditLogs = auditLogs;
    this.users = users;
    this.security = security;
  }

  public void record(String action, String entityType, Object entityId, String oldState, String newState) {
    AuditLog log = new AuditLog();
    try {
      Long userId = security.currentUser().userId();
      User user = users.findById(userId).orElse(null);
      log.user = user;
    } catch (RuntimeException ignored) {
      log.user = null;
    }
    log.action = action;
    log.entityType = entityType;
    log.entityId = String.valueOf(entityId);
    log.oldState = oldState;
    log.newState = newState;
    log.createdAt = LocalDateTime.now();
    auditLogs.save(log);
  }

  public List<AuditLogView> recent() {
    return auditLogs.findTop50ByOrderByCreatedAtDesc().stream()
        .map(log -> new AuditLogView(log.id, log.user == null ? null : log.user.id,
            log.user == null ? "system" : log.user.displayName, log.action, log.entityType,
            log.entityId, log.oldState, log.newState, log.createdAt))
        .toList();
  }
}
