package com.sih.procurement.controller;

import com.sih.procurement.dto.BookingDtos.*;
import com.sih.procurement.dto.AuditDtos.AuditLogView;
import com.sih.procurement.entity.*;
import com.sih.procurement.service.*;
import com.sih.procurement.security.SecuritySupport;
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
  private final AuditService auditService;
  private final SecuritySupport security;

  public BookingController(BookingService bookingService, DashboardService dashboardService, NotificationService notifications,
      AuditService auditService, SecuritySupport security) {
    this.bookingService = bookingService;
    this.dashboardService = dashboardService;
    this.notifications = notifications;
    this.auditService = auditService;
    this.security = security;
  }

  @PostMapping("/slots/book")
  BookingView book(@Valid @RequestBody BookSlotRequest request) { return bookingService.book(request); }

  @PostMapping("/bookings")
  BookingView create(@Valid @RequestBody BookSlotRequest request) { return bookingService.book(request); }

  @GetMapping("/bookings/{id}")
  BookingView booking(@PathVariable Long id) { return bookingService.view(id); }

  @GetMapping("/farmers/{farmerId}/bookings")
  List<BookingView> farmerBookings(@PathVariable Long farmerId) { return bookingService.farmerBookings(farmerId); }

  @GetMapping("/bookings/my")
  List<BookingView> myBookings() { return bookingService.farmerBookings(security.currentUser().farmerId()); }

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

  @PutMapping("/bookings/{id}/cancel")
  BookingView cancel(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
    return bookingService.cancel(id, body == null ? null : body.get("reason"));
  }

  @PutMapping("/bookings/{id}/reschedule")
  BookingView reschedule(@PathVariable Long id, @Valid @RequestBody RescheduleRequest request) {
    return bookingService.reschedule(id, request);
  }

  @PutMapping("/officer/bookings/{id}/call")
  BookingView call(@PathVariable Long id) { return bookingService.updateStatus(id, BookingStatus.CALLED); }

  @PutMapping("/officer/bookings/{id}/arrive")
  BookingView arrive(@PathVariable Long id) { return bookingService.updateStatus(id, BookingStatus.ARRIVED); }

  @PutMapping("/officer/bookings/{id}/verification")
  BookingView verification(@PathVariable Long id) { return bookingService.updateStatus(id, BookingStatus.VERIFICATION); }

  @PutMapping("/officer/bookings/{id}/procurement")
  BookingView procurement(@PathVariable Long id, @Valid @RequestBody(required = false) ProcurementRequest request) {
    if (request != null) bookingService.updateProcurement(id, request);
    return bookingService.updateStatus(id, BookingStatus.PROCUREMENT);
  }

  @PutMapping("/officer/bookings/{id}/complete")
  BookingView complete(@PathVariable Long id, @Valid @RequestBody(required = false) ProcurementRequest request) {
    if (request != null) bookingService.updateProcurement(id, request);
    return bookingService.updateStatus(id, BookingStatus.COMPLETED);
  }

  @PutMapping("/payments/{id}/status")
  BookingView payment(@PathVariable Long id, @RequestBody Map<String, String> body) {
    return bookingService.updatePayment(id, PaymentStatus.valueOf(body.get("status")));
  }

  @GetMapping("/notifications/{userId}")
  List<Notification> userNotifications(@PathVariable Long userId) {
    if (security.currentUser().role() == Role.FARMER && !security.currentUser().userId().equals(userId)) {
      throw new com.sih.procurement.exception.ApiException(org.springframework.http.HttpStatus.FORBIDDEN, "You are not allowed to access these notifications.");
    }
    return notifications.forUser(userId);
  }

  @GetMapping("/admin/dashboard")
  Map<String, Object> dashboard(@RequestParam(defaultValue = "1") Long centreId) { return dashboardService.admin(centreId); }

  @GetMapping("/admin/audit")
  List<AuditLogView> audit() {
    security.requireStaff();
    return auditService.recent();
  }
}
