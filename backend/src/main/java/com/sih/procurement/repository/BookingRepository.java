package com.sih.procurement.repository;

import com.sih.procurement.entity.Booking;
import com.sih.procurement.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface BookingRepository extends JpaRepository<Booking, Long> {
  long countBySlotIdAndStatusNotIn(Long slotId, Collection<BookingStatus> statuses);
  Optional<Booking> findFirstByFarmerIdAndBusinessDateAndStatusNotIn(Long farmerId, LocalDate businessDate, Collection<BookingStatus> statuses);
  List<Booking> findByCentreIdAndBusinessDateOrderByCreatedAtAsc(Long centreId, LocalDate businessDate);
  List<Booking> findByFarmerIdOrderByCreatedAtDesc(Long farmerId);
  long countByCentreIdAndBusinessDateAndStatus(Long centreId, LocalDate businessDate, BookingStatus status);
  long countByCentreIdAndBusinessDateAndStatusIn(Long centreId, LocalDate businessDate, Collection<BookingStatus> statuses);
  List<Booking> findBySlotIdAndStatusNotInOrderByCreatedAtAsc(Long slotId, Collection<BookingStatus> statuses);

  List<Booking> findTop50ByCentreIdAndProcurementStartedAtIsNotNullAndCompletedAtIsNotNullOrderByCompletedAtDesc(Long centreId);
}
