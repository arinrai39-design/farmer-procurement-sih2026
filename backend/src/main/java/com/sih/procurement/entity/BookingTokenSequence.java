package com.sih.procurement.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_token_sequence_centre_date", columnNames = {"centre_id", "business_date"}))
public class BookingTokenSequence {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;
  @ManyToOne(optional = false)
  public ProcurementCentre centre;
  @Column(nullable = false, name = "business_date")
  public LocalDate businessDate;
  @Column(nullable = false, name = "next_value")
  public int nextValue;
}
