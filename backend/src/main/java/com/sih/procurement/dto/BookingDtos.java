package com.sih.procurement.dto;

import com.sih.procurement.entity.BookingStatus;
import com.sih.procurement.entity.PaymentStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BookingDtos {
  public record BookSlotRequest(
      @NotNull Long farmerId,
      @NotNull Long centreId,
      @NotNull Long cropId,
      @NotNull Long slotId,
      @Min(1) int quantityKg) {}

  public record BookingView(
      Long id, String farmerName, String farmerCode, String mobile, String centre,
      String crop, int quantityKg, LocalDate date, String slot, String tokenNumber,
      BookingStatus status, PaymentStatus paymentStatus, BigDecimal amount,
      int queuePosition, int peopleAhead, String estimatedWait) {}
}
