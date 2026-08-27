package com.sih.procurement.config;

import com.sih.procurement.entity.*;
import com.sih.procurement.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Configuration
public class DataSeeder {
  @Value("${app.business-timezone}")
  private String businessTimezone;

  @Value("${app.demo.seed}")
  private boolean demoSeed;

  @Bean
  CommandLineRunner seed(UserRepository users, FarmerRepository farmers, CentreRepository centres,
      CropRepository crops, SlotRepository slots, BookingRepository bookings,
      NotificationRepository notifications, CounterRepository counters, BookingTokenSequenceRepository tokenSequences, PasswordEncoder encoder) {
    return args -> {
      if (!demoSeed) return;
      if (users.count() > 0) return;

      User admin = new User();
      admin.username = "admin";
      admin.displayName = "Centre Officer";
      admin.role = Role.ADMIN;
      admin.passwordHash = encoder.encode("admin123");
      users.save(admin);

      List<ProcurementCentre> centreList = List.of(
          centre("Lucknow Procurement Centre", "Lucknow", "Mandi Parishad Road, Lucknow", "09:00 AM - 05:00 PM", 100),
          centre("Kanpur Procurement Centre", "Kanpur", "GT Road, Kanpur Nagar", "09:00 AM - 05:00 PM", 90),
          centre("Barabanki Procurement Centre", "Barabanki", "Nawabganj Mandi Campus", "08:30 AM - 04:30 PM", 75));
      centres.saveAll(centreList);

      List<Crop> cropList = List.of(crop("Wheat", "21.00"), crop("Rice", "23.50"), crop("Paddy", "22.00"), crop("Maize", "19.25"));
      crops.saveAll(cropList);

      LocalDate demoDate = LocalDate.now(ZoneId.of(businessTimezone));
      for (ProcurementCentre c : centreList) {
        slots.save(slot(c, demoDate, "09:00 AM - 10:00 AM", 10));
        slots.save(slot(c, demoDate, "10:00 AM - 11:00 AM", 10));
        slots.save(slot(c, demoDate, "11:00 AM - 12:00 PM", 10));
        slots.save(slot(c, demoDate, "12:00 PM - 01:00 PM", 10));
        counters.save(counter(c, "Counter 1", admin));
        counters.save(counter(c, "Counter 2", admin));
      }

      String[][] names = {
          {"Rahul Kumar","9876501001","FARM1001","Gosainganj","Lucknow"},
          {"Sunita Devi","9876501002","FARM1002","Malihabad","Lucknow"},
          {"Amit Singh","9876501003","FARM1003","Bithoor","Kanpur"},
          {"Pooja Yadav","9876501004","FARM1004","Safedabad","Barabanki"},
          {"Vikram Patel","9876501005","FARM1005","Chinhat","Lucknow"},
          {"Neha Verma","9876501006","FARM1006","Rura","Kanpur"},
          {"Arjun Pal","9876501007","FARM1007","Dewa","Barabanki"},
          {"Meera Chauhan","9876501008","FARM1008","Mohanlalganj","Lucknow"},
          {"Kiran Nishad","9876501009","FARM1009","Akbarpur","Kanpur"},
          {"Ramesh Gupta","9876501010","FARM1010","Haidergarh","Barabanki"},
          {"Sanjay Maurya","9876501011","FARM1011","Kakori","Lucknow"},
          {"Anita Shukla","9876501012","FARM1012","Jajmau","Kanpur"}};

      List<Slot> allSlots = slots.findAll();
      for (int i = 0; i < names.length; i++) {
        User u = new User();
        u.username = names[i][1];
        u.displayName = names[i][0];
        u.role = Role.FARMER;
        u.passwordHash = encoder.encode("farmer123");
        users.save(u);

        Farmer f = new Farmer();
        f.user = u;
        f.mobile = names[i][1];
        f.farmerCode = names[i][2];
        f.address = names[i][3] + ", " + names[i][4];
        f.village = names[i][3];
        f.district = names[i][4];
        f.state = "Uttar Pradesh";
        farmers.save(f);

        Booking b = new Booking();
        b.farmer = f;
        b.centre = centreList.get(i % centreList.size());
        b.crop = cropList.get(i % cropList.size());
        b.slot = allSlots.get(i % allSlots.size());
        b.businessDate = demoDate;
        b.quantityKg = 1200 + (i * 175);
        b.tokenSequence = 101 + i;
        b.tokenNumber = centreList.get(i % centreList.size()).location.substring(0, 3).toUpperCase() + "-" + b.tokenSequence;
        b.status = i < 2 ? BookingStatus.COMPLETED : i == 2 ? BookingStatus.PROCUREMENT : i == 3 ? BookingStatus.VERIFICATION : BookingStatus.WAITING;
        b.paymentStatus = i < 1 ? PaymentStatus.PAID : i < 4 ? PaymentStatus.PROCESSING : PaymentStatus.PENDING;
        b.ratePerKg = b.crop.ratePerKg;
        b.weighedQuantityKg = b.status == BookingStatus.COMPLETED ? b.quantityKg - 20 : 0;
        b.acceptedQuantityKg = b.status == BookingStatus.COMPLETED ? b.quantityKg - 20 : 0;
        b.procurementAmount = b.status == BookingStatus.COMPLETED ? b.crop.ratePerKg.multiply(BigDecimal.valueOf(b.acceptedQuantityKg)) : null;
        b.createdAt = LocalDateTime.now().minusMinutes(70L - i * 4L);
        b.updatedAt = b.createdAt;
        if (b.status == BookingStatus.COMPLETED) {
          b.calledAt = b.createdAt.plusMinutes(12);
          b.arrivedAt = b.calledAt.plusMinutes(3);
          b.verificationStartedAt = b.arrivedAt.plusMinutes(3);
          b.procurementStartedAt = b.verificationStartedAt.plusMinutes(4);
          b.completedAt = b.procurementStartedAt.plusMinutes(8 + (i % 3));
          if (b.paymentStatus == PaymentStatus.PAID) b.paymentUpdatedAt = b.completedAt.plusMinutes(20);
        }
        bookings.save(b);

        Notification n = new Notification();
        n.user = u;
        n.message = "Your token is " + b.tokenNumber + ". Track queue status from your dashboard.";
        n.createdAt = LocalDateTime.now().minusMinutes(20);
        notifications.save(n);
      }

      for (ProcurementCentre centre : centreList) {
        BookingTokenSequence sequence = new BookingTokenSequence();
        sequence.centre = centre;
        sequence.businessDate = demoDate;
        sequence.nextValue = bookings.findByCentreIdAndBusinessDateOrderByCreatedAtAsc(centre.id, demoDate).stream()
            .mapToInt(b -> b.tokenSequence)
            .max()
            .orElse(100) + 1;
        tokenSequences.save(sequence);
      }
    };
  }

  private ProcurementCentre centre(String name, String location, String address, String hours, int capacity) {
    ProcurementCentre c = new ProcurementCentre();
    c.name = name; c.location = location; c.address = address; c.workingHours = hours; c.dailyCapacity = capacity;
    return c;
  }

  private Crop crop(String name, String rate) {
    Crop c = new Crop();
    c.name = name; c.ratePerKg = new BigDecimal(rate);
    return c;
  }

  private Slot slot(ProcurementCentre centre, LocalDate date, String time, int capacity) {
    Slot s = new Slot();
    s.centre = centre; s.slotDate = date; s.timeRange = time; s.capacity = capacity; s.openFlag = true;
    return s;
  }

  private Counter counter(ProcurementCentre centre, String name, User officer) {
    Counter counter = new Counter();
    counter.centre = centre;
    counter.name = name;
    counter.officer = officer;
    counter.activeFlag = true;
    return counter;
  }
}
