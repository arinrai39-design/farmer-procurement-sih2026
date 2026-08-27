package com.sih.procurement.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "users")
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;
  @Column(nullable = false, unique = true)
  public String username;
  @Column(nullable = false)
  @JsonIgnore
  public String passwordHash;
  @Enumerated(EnumType.STRING)
  public Role role;
  public String displayName;
}
