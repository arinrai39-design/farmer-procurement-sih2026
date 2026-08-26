package com.sih.procurement.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Booking {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;
  @ManyToOne(optional = false)
  public Farmer farmer;
  @ManyToOne(optional = false)
  public ProcurementCentre centre;
  @ManyToOne(optional = false)
  public Crop crop;
  @ManyToOne(optional = false)
  public Slot slot;
  @Column(nullable = false, unique = true)
  public String tokenNumber;
  public int quantityKg;
  @Enumerated(EnumType.STRING)
  public BookingStatus status;
  public LocalDateTime createdAt;
  public BigDecimal procurementAmount;
  @Enumerated(EnumType.STRING)
  public PaymentStatus paymentStatus;
}
