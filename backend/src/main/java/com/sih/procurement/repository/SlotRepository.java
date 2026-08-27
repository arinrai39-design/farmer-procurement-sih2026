package com.sih.procurement.repository;

import com.sih.procurement.entity.Slot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SlotRepository extends JpaRepository<Slot, Long> {
  List<Slot> findByCentreIdAndSlotDate(Long centreId, LocalDate slotDate);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Slot s where s.id = :id")
  Optional<Slot> lockById(@Param("id") Long id);
}
