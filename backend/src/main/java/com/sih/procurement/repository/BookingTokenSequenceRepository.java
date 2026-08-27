package com.sih.procurement.repository;

import com.sih.procurement.entity.BookingTokenSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Optional;

public interface BookingTokenSequenceRepository extends JpaRepository<BookingTokenSequence, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from BookingTokenSequence s where s.centre.id = :centreId and s.businessDate = :businessDate")
  Optional<BookingTokenSequence> lockByCentreAndBusinessDate(@Param("centreId") Long centreId, @Param("businessDate") LocalDate businessDate);
}
