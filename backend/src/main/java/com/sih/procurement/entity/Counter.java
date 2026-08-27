package com.sih.procurement.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "procurement_counter", uniqueConstraints = @UniqueConstraint(name = "uk_counter_centre_name", columnNames = {"centre_id", "name"}))
public class Counter {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;
  @ManyToOne(optional = false)
  public ProcurementCentre centre;
  @Column(nullable = false)
  public String name;
  public boolean activeFlag = true;
  @ManyToOne
  public Booking currentBooking;
  @ManyToOne
  public User officer;
}
