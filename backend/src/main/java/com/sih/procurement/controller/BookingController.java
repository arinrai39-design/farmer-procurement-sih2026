package com.sih.procurement.controller;

import com.sih.procurement.dto.BookingDtos.*;
import com.sih.procurement.entity.*;
import com.sih.procurement.service.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BookingController {
  private final BookingService bookingService;
  private final DashboardService dashboardService;
  private final NotificationService notifications;

  public BookingController(BookingService bookingService, DashboardService dashboardService, NotificationService notifications) {
    this.bookingService = bookingService;
    this.dashboardService = dashboardService;
    this.notifications = notifications;
  }

  @PostMapping("/slots/book")
  BookingView book(@Valid @RequestBody BookSlotRequest request) { return bookingService.book(request); }

  @PostMapping("/bookings")
  BookingView create(@Valid @RequestBody BookSlotRequest request) { return bookingService.book(request); }

  @GetMapping("/bookings/{id}")
  BookingView booking(@PathVariable Long id) { return bookingService.view(id); }

  @GetMapping("/farmers/{farmerId}/bookings")
  List<BookingView> farmerBookings(@PathVariable Long farmerId) { return bookingService.farmerBookings(farmerId); }

  @GetMapping("/queue/{bookingId}")
  BookingView queuePosition(@PathVariable Long bookingId) { return bookingService.view(bookingId); }

  @GetMapping("/admin/queue")
  List<BookingView> queue(@RequestParam(defaultValue = "1") Long centreId) { return bookingService.queue(centreId); }

  @PutMapping("/queue/next")
  BookingView next(@RequestParam(defaultValue = "1") Long centreId) { return bookingService.callNext(centreId); }

  @PutMapping("/procurements/{id}/status")
  BookingView status(@PathVariable Long id, @RequestBody Map<String, String> body) {
    return bookingService.updateStatus(id, BookingStatus.valueOf(body.get("status")));
  }

  @PutMapping("/payments/{id}/status")
  BookingView payment(@PathVariable Long id, @RequestBody Map<String, String> body) {
    return bookingService.updatePayment(id, PaymentStatus.valueOf(body.get("status")));
  }

  @GetMapping("/notifications/{userId}")
  List<Notification> userNotifications(@PathVariable Long userId) { return notifications.forUser(userId); }

  @GetMapping("/admin/dashboard")
  Map<String, Object> dashboard(@RequestParam(defaultValue = "1") Long centreId) { return dashboardService.admin(centreId); }
}
