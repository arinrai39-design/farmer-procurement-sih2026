package com.sih.procurement.dto;

import java.time.LocalDateTime;

public class AuditDtos {
  public record AuditLogView(Long id, Long userId, String userName, String action, String entityType,
      String entityId, String oldState, String newState, LocalDateTime createdAt) {}
}
