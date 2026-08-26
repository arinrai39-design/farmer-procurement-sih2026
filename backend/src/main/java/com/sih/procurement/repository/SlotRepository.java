package com.sih.procurement.repository;

import com.sih.procurement.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface SlotRepository extends JpaRepository<Slot, Long> {
  List<Slot> findByCentreIdAndSlotDate(Long centreId, LocalDate slotDate);
}
