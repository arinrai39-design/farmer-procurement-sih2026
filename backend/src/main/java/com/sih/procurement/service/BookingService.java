package com.sih.procurement.service;

import com.sih.procurement.dto.BookingDtos.*;
import com.sih.procurement.entity.*;
import com.sih.procurement.exception.ApiException;
import com.sih.procurement.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BookingService {
  private final BookingRepository bookings;
  private final FarmerRepository farmers;
  private final CentreRepository centres;
  private final CropRepository crops;
  private final SlotRepository slots;
  private final NotificationService notificationService;

  public BookingService(BookingRepository bookings, FarmerRepository farmers, CentreRepository centres,
      CropRepository crops, SlotRepository slots, NotificationService notificationService) {
    this.bookings = bookings;
    this.farmers = farmers;
    this.centres = centres;
    this.crops = crops;
    this.slots = slots;
    this.notificationService = notificationService;
  }

  @Transactional
  public BookingView book(BookSlotRequest req) {
    Farmer farmer = farmers.findById(req.farmerId()).orElseThrow(() -> new ApiException("Invalid farmer."));
    bookings.findFirstByFarmerIdAndStatusNotIn(farmer.id, List.of(BookingStatus.COMPLETED, BookingStatus.CANCELLED, BookingStatus.SKIPPED))
        .ifPresent(existing -> { throw new ApiException("You already have an active booking."); });
    Slot slot = slots.findById(req.slotId()).orElseThrow(() -> new ApiException("Invalid slot."));
    if (bookings.countBySlotId(slot.id) >= slot.capacity) {
      throw new ApiException("This slot is full. Please select another time.");
    }
    Booking booking = new Booking();
    booking.farmer = farmer;
    booking.centre = centres.findById(req.centreId()).orElseThrow(() -> new ApiException("Invalid centre."));
    booking.crop = crops.findById(req.cropId()).orElseThrow(() -> new ApiException("Invalid crop."));
    booking.slot = slot;
    booking.quantityKg = req.quantityKg();
    booking.tokenNumber = nextToken(booking.centre.id);
    booking.status = BookingStatus.WAITING;
    booking.paymentStatus = PaymentStatus.PENDING;
    booking.createdAt = LocalDateTime.now();
    bookings.save(booking);
    notificationService.send(farmer.user, "Your slot has been booked successfully. Token: " + booking.tokenNumber + ".");
    return view(booking);
  }

  public BookingView view(Long id) {
    return view(bookings.findById(id).orElseThrow(() -> new ApiException("Invalid booking.")));
  }

  public List<BookingView> queue(Long centreId) {
    return bookings.findByCentreIdOrderByCreatedAtAsc(centreId).stream().map(this::view).toList();
  }

  public List<BookingView> farmerBookings(Long farmerId) {
    return bookings.findByFarmerIdOrderByCreatedAtDesc(farmerId).stream().map(this::view).toList();
  }

  @Transactional
  public BookingView callNext(Long centreId) {
    Booking next = bookings.findByCentreIdOrderByCreatedAtAsc(centreId).stream()
        .filter(b -> b.status == BookingStatus.WAITING)
        .findFirst()
        .orElseThrow(() -> new ApiException("No waiting farmers in this queue."));
    next.status = BookingStatus.CALLED;
    notificationService.send(next.farmer.user, "Your turn is now. Please proceed to the procurement counter.");
    return view(next);
  }

  @Transactional
  public BookingView updateStatus(Long id, BookingStatus status) {
    Booking booking = bookings.findById(id).orElseThrow(() -> new ApiException("Invalid token."));
    booking.status = status;
    if (status == BookingStatus.COMPLETED) {
      booking.procurementAmount = booking.crop.ratePerKg.multiply(BigDecimal.valueOf(booking.quantityKg));
      booking.paymentStatus = PaymentStatus.PROCESSING;
      notificationService.send(booking.farmer.user, "Your procurement has been completed. Payment is now processing.");
    } else {
      notificationService.send(booking.farmer.user, "Your procurement status is now " + status + ".");
    }
    return view(booking);
  }

  @Transactional
  public BookingView updatePayment(Long id, PaymentStatus status) {
    Booking booking = bookings.findById(id).orElseThrow(() -> new ApiException("Payment update failed: invalid booking."));
    booking.paymentStatus = status;
    if (booking.procurementAmount == null) {
      booking.procurementAmount = booking.crop.ratePerKg.multiply(BigDecimal.valueOf(booking.quantityKg));
    }
    notificationService.send(booking.farmer.user, "Your payment of Rs " + booking.procurementAmount + " has been marked as " + status + ".");
    return view(booking);
  }

  public BookingView view(Booking b) {
    List<Booking> ordered = bookings.findByCentreIdOrderByCreatedAtAsc(b.centre.id).stream()
        .filter(x -> x.status != BookingStatus.COMPLETED && x.status != BookingStatus.CANCELLED && x.status != BookingStatus.SKIPPED)
        .toList();
    int index = Math.max(0, ordered.indexOf(b));
    int peopleAhead = Math.max(0, index);
    return new BookingView(b.id, b.farmer.user.displayName, b.farmer.farmerCode, b.farmer.mobile,
        b.centre.name, b.crop.name, b.quantityKg, b.slot.slotDate, b.slot.timeRange, b.tokenNumber,
        b.status, b.paymentStatus, b.procurementAmount, peopleAhead + 1, peopleAhead, (peopleAhead * 5 + 5) + " minutes");
  }

  private String nextToken(Long centreId) {
    int next = bookings.findByCentreIdOrderByCreatedAtAsc(centreId).size() + 101;
    return "A" + next;
  }
}
