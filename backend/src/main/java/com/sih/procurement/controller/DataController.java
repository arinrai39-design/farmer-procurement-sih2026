package com.sih.procurement.controller;

import com.sih.procurement.entity.*;
import com.sih.procurement.repository.*;
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

  public DataController(CentreRepository centres, CropRepository crops, SlotRepository slots, BookingRepository bookings, FarmerRepository farmers) {
    this.centres = centres;
    this.crops = crops;
    this.slots = slots;
    this.bookings = bookings;
    this.farmers = farmers;
  }

  @GetMapping("/centres")
  List<ProcurementCentre> centres() { return centres.findAll(); }

  @GetMapping("/centres/{id}")
  ProcurementCentre centre(@PathVariable Long id) { return centres.findById(id).orElseThrow(); }

  @GetMapping("/crops")
  List<Crop> crops() { return crops.findAll(); }

  @GetMapping("/farmers/{id}")
  Farmer farmer(@PathVariable Long id) { return farmers.findById(id).orElseThrow(); }

  @GetMapping("/slots/available")
  List<Map<String, Object>> available(@RequestParam Long centreId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return slots.findByCentreIdAndSlotDate(centreId, date).stream().map(slot -> {
      long booked = bookings.countBySlotId(slot.id);
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("id", slot.id);
      row.put("date", slot.slotDate);
      row.put("timeRange", slot.timeRange);
      row.put("capacity", slot.capacity);
      row.put("booked", booked);
      row.put("available", slot.capacity - booked);
      row.put("full", booked >= slot.capacity);
      return row;
    }).toList();
  }
}
