package com.sih.procurement.entity;

import jakarta.persistence.*;

@Entity
public class ProcurementCentre {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;
  public String name;
  public String location;
  public String address;
  public String workingHours;
  public int dailyCapacity;
}
