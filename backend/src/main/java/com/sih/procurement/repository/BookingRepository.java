package com.sih.procurement.repository;

import com.sih.procurement.entity.Booking;
import com.sih.procurement.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
  long countBySlotId(Long slotId);
  Optional<Booking> findFirstByFarmerIdAndStatusNotIn(Long farmerId, Collection<BookingStatus> statuses);
  List<Booking> findByCentreIdOrderByCreatedAtAsc(Long centreId);
  List<Booking> findByFarmerIdOrderByCreatedAtDesc(Long farmerId);
}
