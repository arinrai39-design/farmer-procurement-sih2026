package com.sih.procurement.service;

import com.sih.procurement.dto.BookingDtos.*;
import com.sih.procurement.entity.*;
import com.sih.procurement.exception.ApiException;
import com.sih.procurement.repository.*;
import com.sih.procurement.security.SecuritySupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BookingService {
  private final BookingRepository bookings;
  private final FarmerRepository farmers;
  private final CentreRepository centres;
  private final CropRepository crops;
  private final SlotRepository slots;
  private final BookingTokenSequenceRepository tokenSequences;
  private final NotificationService notificationService;
  private final QueueIntelligenceService queueIntelligence;
  private final StateTransitionService stateTransitions;
  private final SecuritySupport security;
  private final AuditService auditService;
  private final BusinessClock businessClock;

  public BookingService(BookingRepository bookings, FarmerRepository farmers, CentreRepository centres,
      CropRepository crops, SlotRepository slots, BookingTokenSequenceRepository tokenSequences,
      NotificationService notificationService, QueueIntelligenceService queueIntelligence,
      StateTransitionService stateTransitions, SecuritySupport security, AuditService auditService,
      BusinessClock businessClock) {
    this.bookings = bookings;
    this.farmers = farmers;
    this.centres = centres;
    this.crops = crops;
    this.slots = slots;
    this.tokenSequences = tokenSequences;
    this.notificationService = notificationService;
    this.queueIntelligence = queueIntelligence;
    this.stateTransitions = stateTransitions;
    this.security = security;
    this.auditService = auditService;
    this.businessClock = businessClock;
  }

  @Transactional
  public BookingView book(BookSlotRequest req) {
    security.requireFarmerOwner(req.farmerId());
    Farmer farmer = farmers.findById(req.farmerId()).orElseThrow(() -> new ApiException("Invalid farmer."));
    Slot slot = slots.lockById(req.slotId()).orElseThrow(() -> new ApiException("Invalid slot."));
    LocalDate businessDate = slot.slotDate;
    bookings.findFirstByFarmerIdAndBusinessDateAndStatusNotIn(farmer.id, businessDate, terminalStatuses())
        .ifPresent(existing -> { throw new ApiException("You already have an active booking."); });
    ProcurementCentre centre = centres.findById(req.centreId()).orElseThrow(() -> new ApiException("Invalid centre."));
    Crop crop = crops.findById(req.cropId()).orElseThrow(() -> new ApiException("Invalid crop."));
    if (!slot.centre.id.equals(centre.id)) {
      throw new ApiException("Selected slot does not belong to the selected centre.");
    }
    if (!slot.openFlag) {
      throw new ApiException("This slot is closed. Please select another time.");
    }
    if (bookings.countBySlotIdAndStatusNotIn(slot.id, terminalStatuses()) >= slot.capacity) {
      throw new ApiException("This slot is full. Please select another time.");
    }
    Booking booking = new Booking();
    booking.farmer = farmer;
    booking.centre = centre;
    booking.crop = crop;
    booking.slot = slot;
    booking.businessDate = businessDate;
    booking.quantityKg = req.quantityKg();
    Token token = nextToken(centre, businessDate);
    booking.tokenSequence = token.sequence();
    booking.tokenNumber = token.value();
    booking.status = BookingStatus.WAITING;
    booking.paymentStatus = PaymentStatus.PENDING;
    booking.createdAt = LocalDateTime.now();
    booking.updatedAt = booking.createdAt;
    bookings.save(booking);
    auditService.record("BOOKING_CREATED", "Booking", booking.id, null, booking.tokenNumber);
    notificationService.send(farmer.user, "BOOKING_CONFIRMED", "Your slot has been booked successfully. Token: " + booking.tokenNumber + ".");
    return view(booking);
  }

  public BookingView view(Long id) {
    Booking booking = bookings.findById(id).orElseThrow(() -> new ApiException("Invalid booking."));
    security.requireFarmerOwner(booking.farmer.id);
    return view(booking);
  }

  public List<BookingView> queue(Long centreId) {
    security.requireStaff();
    return bookings.findByCentreIdAndBusinessDateOrderByCreatedAtAsc(centreId, businessClock.today()).stream().map(this::view).toList();
  }

  public List<BookingView> farmerBookings(Long farmerId) {
    security.requireFarmerOwner(farmerId);
    return bookings.findByFarmerIdOrderByCreatedAtDesc(farmerId).stream().map(this::view).toList();
  }

  @Transactional
  public BookingView callNext(Long centreId) {
    security.requireStaff();
    Booking next = bookings.findByCentreIdAndBusinessDateOrderByCreatedAtAsc(centreId, businessClock.today()).stream()
        .filter(b -> b.status == BookingStatus.WAITING)
        .findFirst()
        .orElseThrow(() -> new ApiException("No waiting farmers in this queue."));
    BookingStatus old = next.status;
    stateTransitions.transition(next, BookingStatus.CALLED);
    auditService.record("FARMER_CALLED", "Booking", next.id, old.name(), next.status.name());
    notificationService.send(next.farmer.user, "FARMER_CALLED", "Your turn is now. Please proceed to the procurement counter.");
    return view(next);
  }

  @Transactional
  public BookingView updateStatus(Long id, BookingStatus status) {
    security.requireStaff();
    Booking booking = bookings.findById(id).orElseThrow(() -> new ApiException("Invalid token."));
    BookingStatus old = booking.status;
    stateTransitions.transition(booking, status);
    auditService.record("STATUS_CHANGED", "Booking", booking.id, old.name(), booking.status.name());
    if (booking.status == BookingStatus.COMPLETED) {
      notificationService.send(booking.farmer.user, "PROCUREMENT_COMPLETED", "Your procurement has been completed. Payment is now processing.");
    } else {
      notificationService.send(booking.farmer.user, "STATUS_CHANGED", "Your procurement status is now " + status + ".");
    }
    return view(booking);
  }

  @Transactional
  public BookingView updatePayment(Long id, PaymentStatus status) {
    security.requireStaff();
    Booking booking = bookings.findById(id).orElseThrow(() -> new ApiException("Payment update failed: invalid booking."));
    if (status == PaymentStatus.PAID && booking.status != BookingStatus.PAYMENT_PROCESSING && booking.status != BookingStatus.PAID) {
      if (booking.status == BookingStatus.COMPLETED) {
        stateTransitions.transition(booking, BookingStatus.PAYMENT_PROCESSING);
      } else {
        throw new ApiException("Payment can be marked paid only after procurement is completed.");
      }
    }
    booking.paymentStatus = status;
    if (booking.procurementAmount == null) {
      booking.ratePerKg = booking.crop.ratePerKg;
      booking.acceptedQuantityKg = booking.acceptedQuantityKg > 0 ? booking.acceptedQuantityKg : booking.quantityKg;
      booking.procurementAmount = booking.crop.ratePerKg.multiply(BigDecimal.valueOf(booking.acceptedQuantityKg));
    }
    if (status == PaymentStatus.PAID) booking.status = BookingStatus.PAID;
    booking.paymentUpdatedAt = LocalDateTime.now();
    auditService.record("PAYMENT_UPDATED", "Booking", booking.id, null, status.name());
    notificationService.send(booking.farmer.user, "PAYMENT_UPDATED", "Your payment of Rs " + booking.procurementAmount + " has been marked as " + status + ".");
    return view(booking);
  }

  @Transactional
  public BookingView cancel(Long id, String reason) {
    Booking booking = bookings.findById(id).orElseThrow(() -> new ApiException("Invalid booking."));
    security.requireFarmerOwner(booking.farmer.id);
    BookingStatus old = booking.status;
    stateTransitions.transition(booking, BookingStatus.CANCELLED);
    booking.cancellationReason = reason;
    auditService.record("BOOKING_CANCELLED", "Booking", booking.id, old.name(), BookingStatus.CANCELLED.name());
    notificationService.send(booking.farmer.user, "BOOKING_CANCELLED", "Your booking " + booking.tokenNumber + " has been cancelled.");
    return view(booking);
  }

  @Transactional
  public BookingView reschedule(Long id, RescheduleRequest request) {
    Booking booking = bookings.findById(id).orElseThrow(() -> new ApiException("Invalid booking."));
    security.requireFarmerOwner(booking.farmer.id);
    Slot nextSlot = slots.lockById(request.slotId()).orElseThrow(() -> new ApiException("Invalid slot."));
    ProcurementCentre centre = centres.findById(request.centreId()).orElseThrow(() -> new ApiException("Invalid centre."));
    if (!nextSlot.centre.id.equals(centre.id)) throw new ApiException("Selected slot does not belong to the selected centre.");
    if (!nextSlot.openFlag || bookings.countBySlotIdAndStatusNotIn(nextSlot.id, terminalStatuses()) >= nextSlot.capacity) {
      throw new ApiException("Selected slot is unavailable.");
    }
    String old = booking.centre.name + " " + booking.slot.timeRange;
    booking.centre = centre;
    booking.slot = nextSlot;
    booking.businessDate = nextSlot.slotDate;
    booking.updatedAt = LocalDateTime.now();
    auditService.record("BOOKING_RESCHEDULED", "Booking", booking.id, old, centre.name + " " + nextSlot.timeRange);
    notificationService.send(booking.farmer.user, "SLOT_CHANGED", "Your booking has been rescheduled to " + nextSlot.timeRange + ".");
    return view(booking);
  }

  @Transactional
  public BookingView updateProcurement(Long id, ProcurementRequest request) {
    security.requireStaff();
    Booking booking = bookings.findById(id).orElseThrow(() -> new ApiException("Invalid booking."));
    if (request.acceptedQuantityKg() > request.weighedQuantityKg()) {
      throw new ApiException("Accepted quantity cannot exceed weighed quantity.");
    }
    booking.weighedQuantityKg = request.weighedQuantityKg();
    booking.acceptedQuantityKg = request.acceptedQuantityKg();
    booking.ratePerKg = booking.crop.ratePerKg;
    booking.procurementAmount = booking.ratePerKg.multiply(BigDecimal.valueOf(booking.acceptedQuantityKg));
    booking.updatedAt = LocalDateTime.now();
    auditService.record("QUANTITY_UPDATED", "Booking", booking.id, null, request.acceptedQuantityKg() + "kg");
    return view(booking);
  }

  public BookingView view(Booking b) {
    QueueIntelligenceService.QueueEstimate estimate = queueIntelligence.estimate(b);
    return new BookingView(b.id, b.farmer.user.displayName, b.farmer.farmerCode, b.farmer.mobile,
        b.centre.name, b.crop.name, b.quantityKg, b.slot.slotDate, b.slot.timeRange, b.tokenNumber,
        b.status, b.paymentStatus, b.procurementAmount, estimate.queuePosition(), estimate.peopleAhead(),
        estimate.displayWait(), estimate.waitMinutes(), estimate.congestion(), estimate.confidence(),
        estimate.averageServiceMinutes(), estimate.activeCounters(), b.weighedQuantityKg, b.acceptedQuantityKg, b.ratePerKg);
  }

  private Token nextToken(ProcurementCentre centre, LocalDate businessDate) {
    BookingTokenSequence sequence = tokenSequences.lockByCentreAndBusinessDate(centre.id, businessDate).orElseGet(() -> {
      BookingTokenSequence created = new BookingTokenSequence();
      created.centre = centre;
      created.businessDate = businessDate;
      created.nextValue = 101;
      return tokenSequences.saveAndFlush(created);
    });
    int value = sequence.nextValue++;
    String prefix = centre.location == null || centre.location.isBlank() ? "CTR" : centre.location.substring(0, Math.min(3, centre.location.length())).toUpperCase(Locale.ROOT);
    return new Token(value, prefix + "-" + value);
  }

  private List<BookingStatus> terminalStatuses() {
    return List.of(BookingStatus.COMPLETED, BookingStatus.CANCELLED, BookingStatus.SKIPPED, BookingStatus.NO_SHOW, BookingStatus.PAID);
  }

  private record Token(int sequence, String value) {}
}
