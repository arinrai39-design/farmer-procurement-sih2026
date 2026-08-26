package com.sih.procurement.service;

import com.sih.procurement.entity.*;
import com.sih.procurement.repository.*;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;

@Service
public class DashboardService {
  private final BookingRepository bookings;
  private final CropRepository crops;

  public DashboardService(BookingRepository bookings, CropRepository crops) {
    this.bookings = bookings;
    this.crops = crops;
  }

  public Map<String, Object> admin(Long centreId) {
    var all = bookings.findByCentreIdOrderByCreatedAtAsc(centreId);
    BigDecimal pending = all.stream()
        .filter(b -> b.paymentStatus != PaymentStatus.PAID)
        .map(b -> b.procurementAmount == null ? BigDecimal.ZERO : b.procurementAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    Map<String, Integer> cropWise = new LinkedHashMap<>();
    for (Crop crop : crops.findAll()) {
      cropWise.put(crop.name, all.stream().filter(b -> b.crop.id.equals(crop.id)).mapToInt(b -> b.quantityKg).sum());
    }
    return Map.of(
        "todaysFarmers", all.size(),
        "waiting", all.stream().filter(b -> b.status == BookingStatus.WAITING || b.status == BookingStatus.CALLED).count(),
        "processing", all.stream().filter(b -> b.status == BookingStatus.VERIFICATION || b.status == BookingStatus.PROCUREMENT).count(),
        "completed", all.stream().filter(b -> b.status == BookingStatus.COMPLETED).count(),
        "pendingPayments", pending,
        "farmersPerDay", List.of(31, 42, 47, 39, all.size()),
        "paymentStatus", Map.of(
            "pending", all.stream().filter(b -> b.paymentStatus == PaymentStatus.PENDING).count(),
            "processing", all.stream().filter(b -> b.paymentStatus == PaymentStatus.PROCESSING).count(),
            "paid", all.stream().filter(b -> b.paymentStatus == PaymentStatus.PAID).count()),
        "cropWiseQuantity", cropWise);
  }
}
