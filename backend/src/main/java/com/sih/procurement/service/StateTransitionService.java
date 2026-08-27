package com.sih.procurement.service;

import com.sih.procurement.entity.Booking;
import com.sih.procurement.entity.BookingStatus;
import com.sih.procurement.entity.PaymentStatus;
import com.sih.procurement.exception.ApiException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class StateTransitionService {
  private static final Map<BookingStatus, Set<BookingStatus>> ALLOWED = Map.ofEntries(
      Map.entry(BookingStatus.WAITING, Set.of(BookingStatus.CALLED, BookingStatus.CANCELLED, BookingStatus.SKIPPED, BookingStatus.NO_SHOW)),
      Map.entry(BookingStatus.CALLED, Set.of(BookingStatus.ARRIVED, BookingStatus.SKIPPED, BookingStatus.NO_SHOW)),
      Map.entry(BookingStatus.ARRIVED, Set.of(BookingStatus.VERIFICATION, BookingStatus.SKIPPED)),
      Map.entry(BookingStatus.VERIFICATION, Set.of(BookingStatus.PROCUREMENT, BookingStatus.SKIPPED)),
      Map.entry(BookingStatus.PROCUREMENT, Set.of(BookingStatus.COMPLETED)),
      Map.entry(BookingStatus.COMPLETED, Set.of(BookingStatus.PAYMENT_PROCESSING)),
      Map.entry(BookingStatus.PAYMENT_PROCESSING, Set.of(BookingStatus.PAID)),
      Map.entry(BookingStatus.PAID, Set.of()),
      Map.entry(BookingStatus.CANCELLED, Set.of()),
      Map.entry(BookingStatus.SKIPPED, Set.of()),
      Map.entry(BookingStatus.NO_SHOW, Set.of()));

  public void transition(Booking booking, BookingStatus next) {
    BookingStatus current = booking.status;
    if (current == next) return;
    if (!ALLOWED.getOrDefault(current, Set.of()).contains(next)) {
      throw new ApiException("Invalid status transition: " + current + " to " + next + ".");
    }
    booking.status = next;
    booking.updatedAt = LocalDateTime.now();
    switch (next) {
      case CALLED -> booking.calledAt = LocalDateTime.now();
      case ARRIVED -> booking.arrivedAt = LocalDateTime.now();
      case VERIFICATION -> booking.verificationStartedAt = LocalDateTime.now();
      case PROCUREMENT -> booking.procurementStartedAt = LocalDateTime.now();
      case COMPLETED -> completeProcurement(booking);
      case PAYMENT_PROCESSING -> booking.paymentStatus = PaymentStatus.PROCESSING;
      case PAID -> {
        booking.paymentStatus = PaymentStatus.PAID;
        booking.paymentUpdatedAt = LocalDateTime.now();
      }
      case CANCELLED -> booking.cancelledAt = LocalDateTime.now();
      default -> {}
    }
  }

  private void completeProcurement(Booking booking) {
    booking.completedAt = LocalDateTime.now();
    int accepted = booking.acceptedQuantityKg > 0 ? booking.acceptedQuantityKg : booking.quantityKg;
    if (booking.weighedQuantityKg > 0 && accepted > booking.weighedQuantityKg) {
      throw new ApiException("Accepted quantity cannot exceed weighed quantity.");
    }
    booking.acceptedQuantityKg = accepted;
    booking.ratePerKg = booking.crop.ratePerKg;
    booking.procurementAmount = booking.ratePerKg.multiply(BigDecimal.valueOf(accepted));
    booking.paymentStatus = PaymentStatus.PROCESSING;
  }
}
