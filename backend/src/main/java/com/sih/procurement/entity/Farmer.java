package com.sih.procurement.entity;

import jakarta.persistence.*;

@Entity
public class Farmer {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;
  @OneToOne(optional = false)
  public User user;
  @Column(nullable = false, unique = true)
  public String farmerCode;
  @Column(nullable = false, unique = true)
  public String mobile;
  public String address;
  public String village;
  public String district;
  public String state;
}
