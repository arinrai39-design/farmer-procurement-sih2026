package com.sih.procurement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Notification {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;
  @ManyToOne(optional = false)
  public User user;
  public String message;
  public boolean readFlag;
  public LocalDateTime createdAt;
}
