package com.sih.procurement.service;

import com.sih.procurement.entity.Booking;
import com.sih.procurement.entity.BookingStatus;
import com.sih.procurement.repository.BookingRepository;
import com.sih.procurement.repository.CounterRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class QueueIntelligenceService {
  private static final List<BookingStatus> ACTIVE = List.of(
      BookingStatus.WAITING, BookingStatus.CALLED, BookingStatus.ARRIVED,
      BookingStatus.VERIFICATION, BookingStatus.PROCUREMENT, BookingStatus.PAYMENT_PROCESSING);

  private final BookingRepository bookings;
  private final CounterRepository counters;

  public QueueIntelligenceService(BookingRepository bookings, CounterRepository counters) {
    this.bookings = bookings;
    this.counters = counters;
  }

  public QueueEstimate estimate(Booking booking) {
    List<Booking> active = bookings.findByCentreIdAndBusinessDateOrderByCreatedAtAsc(booking.centre.id, booking.businessDate).stream()
        .filter(b -> ACTIVE.contains(b.status))
        .toList();
    int index = Math.max(0, active.indexOf(booking));
    int peopleAhead = Math.max(0, index);
    double averageMinutes = averageServiceMinutes(booking.centre.id);
    long activeCounters = Math.max(1, counters.countByCentreIdAndActiveFlagTrue(booking.centre.id));
    long waiting = active.stream().filter(b -> b.status == BookingStatus.WAITING || b.status == BookingStatus.CALLED).count();
    double congestionFactor = waiting > booking.centre.dailyCapacity * 0.7 ? 1.25 : waiting > booking.centre.dailyCapacity * 0.4 ? 1.1 : 1.0;
    int waitMinutes = (int) Math.ceil(((peopleAhead + 1) * averageMinutes / activeCounters) * congestionFactor);
    String confidence = bookings.findTop50ByCentreIdAndProcurementStartedAtIsNotNullAndCompletedAtIsNotNullOrderByCompletedAtDesc(booking.centre.id).isEmpty()
        ? "LOW"
        : waiting > 0 ? "MEDIUM" : "HIGH";
    return new QueueEstimate(peopleAhead + 1, peopleAhead, waitMinutes, averageMinutes, activeCounters, congestion(waiting, booking.centre.dailyCapacity), confidence);
  }

  public double averageServiceMinutes(Long centreId) {
    List<Booking> samples = bookings.findTop50ByCentreIdAndProcurementStartedAtIsNotNullAndCompletedAtIsNotNullOrderByCompletedAtDesc(centreId);
    if (samples.isEmpty()) return 8.0;
    double minutes = samples.stream()
        .mapToLong(b -> java.time.Duration.between(b.procurementStartedAt, b.completedAt).toMinutes())
        .average()
        .orElse(8.0);
    return Math.max(3.0, minutes);
  }

  public String congestion(long queueSize, int dailyCapacity) {
    if (dailyCapacity <= 0) return "MODERATE";
    double ratio = (double) queueSize / dailyCapacity;
    if (ratio >= 0.75) return "CRITICAL";
    if (ratio >= 0.5) return "HIGH";
    if (ratio >= 0.25) return "MODERATE";
    return "LOW";
  }

  public record QueueEstimate(int queuePosition, int peopleAhead, int waitMinutes, double averageServiceMinutes,
      long activeCounters, String congestion, String confidence) {
    public String displayWait() {
      return "~" + waitMinutes + " minutes";
    }

    public LocalDateTime estimatedTurn(LocalDate businessDate) {
      return LocalDateTime.now().plusMinutes(waitMinutes);
    }
  }
}
