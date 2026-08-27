package com.sih.procurement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(indexes = {
    @Index(name = "idx_audit_entity", columnList = "entity_type,entity_id"),
    @Index(name = "idx_audit_created", columnList = "created_at")
})
public class AuditLog {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;
  @ManyToOne
  public User user;
  @Column(nullable = false, length = 80)
  public String action;
  @Column(nullable = false, length = 80, name = "entity_type")
  public String entityType;
  @Column(nullable = false, length = 80, name = "entity_id")
  public String entityId;
  @Column(length = 2000, name = "old_state")
  public String oldState;
  @Column(length = 2000, name = "new_state")
  public String newState;
  @Column(name = "request_metadata")
  public String requestMetadata;
  @Column(nullable = false, name = "created_at")
  public LocalDateTime createdAt;
}
