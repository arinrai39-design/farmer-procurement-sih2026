package com.sih.procurement.controller;

import com.sih.procurement.entity.*;
import com.sih.procurement.dto.BookingDtos.SlotAvailabilityView;
import com.sih.procurement.repository.*;
import com.sih.procurement.service.QueueIntelligenceService;
import com.sih.procurement.security.SecuritySupport;
import com.sih.procurement.exception.ApiException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api")
public class DataController {
  private final CentreRepository centres;
  private final CropRepository crops;
  private final SlotRepository slots;
  private final BookingRepository bookings;
  private final FarmerRepository farmers;
  private final QueueIntelligenceService queueIntelligence;
  private final SecuritySupport security;

  public DataController(CentreRepository centres, CropRepository crops, SlotRepository slots, BookingRepository bookings, FarmerRepository farmers,
      QueueIntelligenceService queueIntelligence, SecuritySupport security) {
    this.centres = centres;
    this.crops = crops;
    this.slots = slots;
    this.bookings = bookings;
    this.farmers = farmers;
    this.queueIntelligence = queueIntelligence;
    this.security = security;
  }

  @GetMapping("/centres")
  List<ProcurementCentre> centres() { return centres.findAll(); }

  @GetMapping("/centres/{id}")
  ProcurementCentre centre(@PathVariable Long id) {
    return centres.findById(id).orElseThrow(() -> new ApiException("Invalid centre."));
  }

  @GetMapping("/crops")
  List<Crop> crops() { return crops.findAll(); }

  @GetMapping("/farmers/{id}")
  Farmer farmer(@PathVariable Long id) {
    security.requireFarmerOwner(id);
    return farmers.findById(id).orElseThrow();
  }

  @GetMapping("/slots/available")
  List<SlotAvailabilityView> available(@RequestParam Long centreId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    List<SlotAvailabilityView> rows = slots.findByCentreIdAndSlotDate(centreId, date).stream().map(slot -> {
      long booked = bookings.countBySlotIdAndStatusNotIn(slot.id, List.of(BookingStatus.CANCELLED, BookingStatus.SKIPPED, BookingStatus.NO_SHOW));
      long available = Math.max(0, slot.capacity - booked);
      int wait = (int) Math.ceil((booked + 1) * queueIntelligence.averageServiceMinutes(centreId));
      return new SlotAvailabilityView(slot.id, slot.slotDate, slot.timeRange, slot.capacity, booked, available,
          available <= 0 || !slot.openFlag, queueIntelligence.congestion(booked, Math.max(1, slot.capacity)), wait, false, "");
    }).toList();
    Optional<SlotAvailabilityView> best = rows.stream().filter(row -> !row.full())
        .min(Comparator.comparingInt(SlotAvailabilityView::estimatedWaitMinutes)
            .thenComparing(SlotAvailabilityView::booked));
    return rows.stream().map(row -> best.isPresent() && row.id().equals(best.get().id())
        ? new SlotAvailabilityView(row.id(), row.date(), row.timeRange(), row.capacity(), row.booked(), row.available(),
            row.full(), row.congestion(), row.estimatedWaitMinutes(), true, "Lower predicted queue and waiting time.")
        : row).toList();
  }
}
