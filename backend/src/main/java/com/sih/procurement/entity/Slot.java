package com.sih.procurement.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_slot_centre_date_time", columnNames = {"centre_id", "slot_date", "time_range"}),
    indexes = @Index(name = "idx_slot_centre_date", columnList = "centre_id,slot_date"))
public class Slot {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;
  @ManyToOne(optional = false)
  public ProcurementCentre centre;
  @Column(name = "slot_date")
  public LocalDate slotDate;
  @Column(name = "time_range")
  public String timeRange;
  public int capacity;
  public boolean openFlag = true;
}
