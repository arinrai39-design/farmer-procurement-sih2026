package com.sih.procurement.service;

import com.sih.procurement.entity.*;
import com.sih.procurement.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class StateTransitionServiceTest {
  private final StateTransitionService service = new StateTransitionService();

  @Test
  void rejectsInvalidStatusTransition() {
    Booking booking = new Booking();
    booking.status = BookingStatus.WAITING;

    assertThrows(ApiException.class, () -> service.transition(booking, BookingStatus.PROCUREMENT));
  }

  @Test
  void completesProcurementUsingAcceptedQuantityAndCropRate() {
    Crop crop = new Crop();
    crop.ratePerKg = new BigDecimal("21.00");
    Booking booking = new Booking();
    booking.crop = crop;
    booking.quantityKg = 1000;
    booking.weighedQuantityKg = 950;
    booking.acceptedQuantityKg = 930;
    booking.status = BookingStatus.PROCUREMENT;

    service.transition(booking, BookingStatus.COMPLETED);

    assertEquals(BookingStatus.COMPLETED, booking.status);
    assertEquals(PaymentStatus.PROCESSING, booking.paymentStatus);
    assertEquals(new BigDecimal("19530.00"), booking.procurementAmount);
  }
}
