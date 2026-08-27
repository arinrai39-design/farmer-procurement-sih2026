package com.sih.procurement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(indexes = @Index(name = "idx_notification_user_created", columnList = "user_id,created_at"))
public class Notification {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;
  @ManyToOne(optional = false)
  public User user;
  public String message;
  public String type;
  public boolean readFlag;
  @Column(name = "created_at")
  public LocalDateTime createdAt;
}
