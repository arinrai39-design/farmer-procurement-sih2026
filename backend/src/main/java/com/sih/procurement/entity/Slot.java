package com.sih.procurement.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Slot {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;
  @ManyToOne(optional = false)
  public ProcurementCentre centre;
  public LocalDate slotDate;
  public String timeRange;
  public int capacity;
}
