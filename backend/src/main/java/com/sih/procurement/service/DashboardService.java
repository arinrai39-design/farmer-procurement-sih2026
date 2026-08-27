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
  private final BusinessClock businessClock;

  public DashboardService(BookingRepository bookings, CropRepository crops, BusinessClock businessClock) {
    this.bookings = bookings;
    this.crops = crops;
    this.businessClock = businessClock;
  }

  public Map<String, Object> admin(Long centreId) {
    var all = bookings.findByCentreIdAndBusinessDateOrderByCreatedAtAsc(centreId, businessClock.today());
    BigDecimal pending = all.stream()
        .filter(b -> b.paymentStatus != PaymentStatus.PAID)
        .map(b -> b.procurementAmount == null ? BigDecimal.ZERO : b.procurementAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    Map<String, Integer> cropWise = new LinkedHashMap<>();
    for (Crop crop : crops.findAll()) {
      cropWise.put(crop.name, all.stream().filter(b -> b.crop.id.equals(crop.id)).mapToInt(b -> b.quantityKg).sum());
    }
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("todaysFarmers", all.size());
    response.put("waiting", all.stream().filter(b -> b.status == BookingStatus.WAITING || b.status == BookingStatus.CALLED).count());
    response.put("processing", all.stream().filter(b -> b.status == BookingStatus.VERIFICATION || b.status == BookingStatus.PROCUREMENT).count());
    response.put("completed", all.stream().filter(b -> b.status == BookingStatus.COMPLETED).count());
    response.put("noShows", all.stream().filter(b -> b.status == BookingStatus.NO_SHOW || b.status == BookingStatus.SKIPPED).count());
    response.put("cancelled", all.stream().filter(b -> b.status == BookingStatus.CANCELLED).count());
    response.put("averageWaitMinutes", all.stream().filter(b -> b.calledAt != null).mapToLong(b -> java.time.Duration.between(b.createdAt, b.calledAt).toMinutes()).average().orElse(0));
    response.put("averageServiceMinutes", all.stream().filter(b -> b.procurementStartedAt != null && b.completedAt != null).mapToLong(b -> java.time.Duration.between(b.procurementStartedAt, b.completedAt).toMinutes()).average().orElse(0));
    response.put("pendingPayments", pending);
    response.put("farmersPerDay", hourlySeries(all));
    response.put("paymentStatus", Map.of(
        "pending", all.stream().filter(b -> b.paymentStatus == PaymentStatus.PENDING).count(),
        "processing", all.stream().filter(b -> b.paymentStatus == PaymentStatus.PROCESSING).count(),
        "paid", all.stream().filter(b -> b.paymentStatus == PaymentStatus.PAID).count()));
    response.put("cropWiseQuantity", cropWise);
    return response;
  }

  private List<Map<String, Object>> hourlySeries(List<Booking> bookings) {
    Map<Integer, Long> byHour = new TreeMap<>();
    for (Booking booking : bookings) {
      byHour.merge(booking.createdAt.getHour(), 1L, Long::sum);
    }
    return byHour.entrySet().stream()
        .map(entry -> Map.<String, Object>of("hour", String.format("%02d:00", entry.getKey()), "bookings", entry.getValue()))
        .toList();
  }
}
