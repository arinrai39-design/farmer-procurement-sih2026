package com.sih.procurement.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Crop {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;
  public String name;
  public BigDecimal ratePerKg;
}
