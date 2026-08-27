package com.sih.procurement.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(indexes = {
    @Index(name = "idx_booking_centre_date_status_created", columnList = "centre_id,business_date,status,created_at"),
    @Index(name = "idx_booking_farmer_status", columnList = "farmer_id,status"),
    @Index(name = "idx_booking_slot_date", columnList = "slot_id,business_date")
})
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
  @Column(nullable = false, name = "business_date")
  public LocalDate businessDate;
  @Column(nullable = false, name = "token_sequence")
  public int tokenSequence;
  @Column(nullable = false, unique = true, length = 40)
  public String tokenNumber;
  public int quantityKg;
  @Enumerated(EnumType.STRING)
  public BookingStatus status;
  @Column(name = "created_at")
  public LocalDateTime createdAt;
  @Column(name = "updated_at")
  public LocalDateTime updatedAt;
  @Column(name = "called_at")
  public LocalDateTime calledAt;
  @Column(name = "arrived_at")
  public LocalDateTime arrivedAt;
  @Column(name = "verification_started_at")
  public LocalDateTime verificationStartedAt;
  @Column(name = "procurement_started_at")
  public LocalDateTime procurementStartedAt;
  @Column(name = "completed_at")
  public LocalDateTime completedAt;
  @Column(name = "cancelled_at")
  public LocalDateTime cancelledAt;
  @Column(name = "cancellation_reason")
  public String cancellationReason;
  @Column(name = "weighed_quantity_kg")
  public int weighedQuantityKg;
  @Column(name = "accepted_quantity_kg")
  public int acceptedQuantityKg;
  @Column(name = "rate_per_kg")
  public BigDecimal ratePerKg;
  @Column(name = "procurement_amount")
  public BigDecimal procurementAmount;
  @Column(name = "payment_reference")
  public String paymentReference;
  @Column(name = "payment_updated_at")
  public LocalDateTime paymentUpdatedAt;
  @Enumerated(EnumType.STRING)
  public PaymentStatus paymentStatus;
}
